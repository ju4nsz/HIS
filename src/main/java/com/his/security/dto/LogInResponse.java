package com.his.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LogInResponse {

    private String token;
    private String tipo = "Bearer";
    private String email;
    private String rol;

}
