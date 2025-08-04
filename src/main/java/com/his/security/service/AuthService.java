package com.his.security.service;

import com.his.security.dto.*;
import jakarta.validation.Valid;

public interface AuthService {
    void registerUser(@Valid RegisterRequest request);

    LogInResponse login(@Valid LogInRequest request);

    RefreshTokenResponse refresh(@Valid String refreshToken);

    void logout(@Valid LogOutRequest request);
}
