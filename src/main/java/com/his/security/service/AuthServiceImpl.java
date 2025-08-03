package com.his.security.service;

import com.his.security.dto.LogInRequest;
import com.his.security.dto.LogInResponse;
import com.his.security.dto.RegisterRequest;
import com.his.security.entity.Permiso;
import com.his.security.entity.Rol;
import com.his.security.entity.Usuario;
import com.his.security.exception.ApiException;
import com.his.security.repository.RolRepository;
import com.his.security.repository.UsuarioRepository;
import com.his.security.util.Constants;
import com.his.security.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void registerUser(RegisterRequest request) {

        if (usuarioRepository.existsByEmail(request.getEmail()))
            throw new ApiException(Constants.Mensajes.USUARIO_EXISTENTE, HttpStatus.CONFLICT);

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

        // Extraer nombres de permisos
        Set<String> permisos = usuario.getRol().getPermisos().stream()
                .map(Permiso::getNombre)
                .collect(Collectors.toSet());

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().getNombreRol(), permisos);

        return new LogInResponse(token, "Bearer", usuario.getEmail(), usuario.getRol().getNombreRol());
    }

}
