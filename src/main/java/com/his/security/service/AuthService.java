package com.his.security.service;

import com.his.security.dto.LogInRequest;
import com.his.security.dto.LogInResponse;
import com.his.security.dto.RegisterRequest;
import jakarta.validation.Valid;

public interface AuthService {
    void registerUser(@Valid RegisterRequest request);

    LogInResponse login(@Valid LogInRequest request);
}
