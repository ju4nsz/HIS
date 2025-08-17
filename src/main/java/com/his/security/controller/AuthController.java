package com.his.security.controller;

import com.his.security.dto.*;
import com.his.security.service.AuthService;
import com.his.security.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Flujos de login/registro/refresh/logout")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Registrarse",
            description = "Devuelve una respuesta estándar.",
            security = {}
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody @Valid RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok(new ApiResponse<>(true, Constants.Mensajes.USUARIO_REGISTRADO, null));
    }

    @Operation(
            summary = "Inicia sesión",
            description = "Devuelve tokens e información relevante del usuario.",
            security = {}
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LogInResponse>> login(@Valid @RequestBody LogInRequest request) {
        LogInResponse response = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, Constants.Mensajes.LOGIN_EXITOSO, response));
    }

    @Operation(
            summary = "Refrescar token de acceso",
            description = "Recibe el refresh token y envía un nuevo token de acceso y otro refresh token.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(new ApiResponse<>(true, Constants.Mensajes.LOGIN_EXITOSO, response));
    }

    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida todos los refresh tokens para cerrar sesión en todos los dispositivos.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogOutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new ApiResponse<>(true, Constants.Mensajes.LOGOUT_EXITOSO, null));
    }

}
