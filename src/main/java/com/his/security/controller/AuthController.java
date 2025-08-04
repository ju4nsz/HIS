package com.his.security.controller;

import com.his.security.dto.*;
import com.his.security.service.AuthService;
import com.his.security.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody @Valid RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok(new ApiResponse<>(true, Constants.Mensajes.USUARIO_REGISTRADO, null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LogInResponse>> login(@Valid @RequestBody LogInRequest request) {
        LogInResponse response = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, Constants.Mensajes.LOGIN_EXITOSO, response));
    }

    @PostMapping("/refresh/{refreshToken}")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(new ApiResponse<>(true, Constants.Mensajes.LOGIN_EXITOSO, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogOutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new ApiResponse<>(true, Constants.Mensajes.LOGOUT_EXITOSO, null));
    }

}
