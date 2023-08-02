package com.example.demo.payLoad.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginResponse {
    @NotBlank
    private String accessToken;
    @NotBlank
    private String refreshToken;
    private String tokenType = "Bearer";

    public LoginResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
