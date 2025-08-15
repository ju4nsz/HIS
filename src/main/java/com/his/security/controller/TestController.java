package com.his.security.controller;

import com.his.security.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Operation(hidden = true)
    @PreAuthorize("hasAuthority('paciente:ver')")
    @GetMapping("/protected")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(new ApiResponse<>(true, "¡Felicidades! Pudiste acceder a este endpoint.", "Datos falsos"));
    }

    @Operation(hidden = true)
    @GetMapping("/non-protected")
    public ResponseEntity<ApiResponse<String>> test2() {
        return ResponseEntity.ok(new ApiResponse<>(true, "¡Felicidades! Pudiste acceder a este endpoint 2.", "Datos falsos 2"));
    }

}
