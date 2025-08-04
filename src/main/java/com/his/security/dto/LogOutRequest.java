package com.his.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogOutRequest {

    @NotBlank(message = "El refresh token es obligatorio.")
    private String refreshToken;

    private Boolean logOutAllDevices = false;

}
