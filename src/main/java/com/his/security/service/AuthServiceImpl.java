package com.his.security.service;

import com.his.security.dto.*;
import com.his.security.entity.Permiso;
import com.his.security.entity.RefreshToken;
import com.his.security.entity.Rol;
import com.his.security.entity.Usuario;
import com.his.security.exception.ApiException;
import com.his.security.repository.RefreshTokenRepository;
import com.his.security.repository.RolRepository;
import com.his.security.repository.UsuarioRepository;
import com.his.security.util.Constants;
import com.his.security.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void registerUser(RegisterRequest request) {

        if (usuarioRepository.existsByEmail(request.getEmail())){
            log.info("Email existente: {}", request.getEmail());
            throw new ApiException(Constants.Mensajes.USUARIO_EXISTENTE, HttpStatus.CONFLICT);
        }

        Rol rol = rolRepository.findByNombreRol(Constants.Roles.RECEPCIONISTA)
                .orElseThrow(() -> new ApiException("Rol por defecto no encontrado", HttpStatus.INTERNAL_SERVER_ERROR));

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .rol(rol)
                .estado(Constants.EstadoUsuario.ACTIVO)
                .fechaCreacion(LocalDateTime.now())
                .nombreCompleto(request.getNombreCompleto())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        usuarioRepository.save(usuario);

        log.info("¡Usuario registrado exitosamente! Email: {}", usuario.getEmail());
    }

    @Override
    public LogInResponse login(LogInRequest request) {

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(Constants.Mensajes.ERROR_CREDENCIALES, HttpStatus.BAD_REQUEST));

        if (!Objects.equals(usuario.getEstado(), Constants.EstadoUsuario.ACTIVO))
            throw new ApiException(Constants.Mensajes.USUARIO_INACTIVO, HttpStatus.BAD_REQUEST);

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new ApiException(Constants.Mensajes.ERROR_CREDENCIALES, HttpStatus.BAD_REQUEST);
        }

        TokenPair tokens = generarTokens(usuario);

        RefreshToken entity = new RefreshToken(null, usuario, tokens.getRefreshToken(),
                LocalDateTime.now().plusDays(Constants.Params.REFRESH_TOKEN_DIAS), true, LocalDateTime.now());
        refreshTokenRepository.save(entity);

        return new LogInResponse(tokens.getRefreshToken(), tokens.getAccessToken(), "Bearer",
                usuario.getEmail(), usuario.getRol().getNombreRol());
    }

    @Transactional
    @Override
    public RefreshTokenResponse refresh(String refreshToken) {

        if (!jwtUtil.isRefreshToken(refreshToken))
            throw new ApiException(Constants.Mensajes.TOKEN_INVALIDO, HttpStatus.UNAUTHORIZED);

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ApiException(Constants.Mensajes.TOKEN_INVALIDO, HttpStatus.UNAUTHORIZED));

        if (!refreshTokenEntity.getActivo() || refreshTokenEntity.getExpiracion().isBefore(LocalDateTime.now()))
            throw new ApiException(Constants.Mensajes.TOKEN_INVALIDO, HttpStatus.UNAUTHORIZED);

        Usuario usuario = refreshTokenEntity.getUsuario();

        TokenPair tokens = generarTokens(usuario);

        refreshTokenEntity.setActivo(false);
        refreshTokenRepository.save(refreshTokenEntity);

        RefreshToken nuevo = new RefreshToken(null, usuario, tokens.getRefreshToken(),
                LocalDateTime.now().plusDays(Constants.Params.REFRESH_TOKEN_DIAS), true, LocalDateTime.now());
        refreshTokenRepository.save(nuevo);

        return new RefreshTokenResponse(tokens.getAccessToken(), tokens.getRefreshToken(), "Bearer");
    }

    @Transactional
    @Override
    public void logout(LogOutRequest request) {

        if (!jwtUtil.isRefreshToken(request.getRefreshToken()))
            throw new ApiException(Constants.Mensajes.TOKEN_INVALIDO, HttpStatus.UNAUTHORIZED);

        RefreshToken tokenEntity = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ApiException("Token no reconocido o ya inválido", HttpStatus.UNAUTHORIZED));

        if (!tokenEntity.getActivo() || tokenEntity.getExpiracion().isBefore(LocalDateTime.now()))
            throw new ApiException("Token expirado o ya invalidado", HttpStatus.UNAUTHORIZED);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailAuth = auth.getName();

        if (!emailAuth.equals(tokenEntity.getUsuario().getEmail()))
            throw new ApiException("El token no pertenece al usuario autenticado", HttpStatus.UNAUTHORIZED);

        if (request.getLogOutAllDevices()){
            refreshTokenRepository.deactivateAllByUserId(tokenEntity.getUsuario().getId());
        } else {
            refreshTokenRepository.deactivateByToken(tokenEntity.getToken());
        }

    }

    private TokenPair generarTokens(Usuario usuario) {
        Set<String> permisos = usuario.getRol().getPermisos().stream()
                .map(Permiso::getNombre)
                .collect(Collectors.toSet());

        String accessToken = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().getNombreRol(), permisos, false);
        String refreshToken = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().getNombreRol(), permisos, true);

        return new TokenPair(accessToken, refreshToken);
    }

}
