package com.his.security.controller;

import com.his.security.dto.ApiResponse;
import com.his.security.dto.LogInRequest;
import com.his.security.dto.LogInResponse;
import com.his.security.dto.RegisterRequest;
import com.his.security.service.AuthService;
import com.his.security.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
