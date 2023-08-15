package com.example.demo.payLoad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenResponse {
    @NotBlank
    private String accessToken;
    @NotBlank
    private String refreshToken;
    @NotBlank
    private String messageRefreshToken;
    private String tokenType = "Bearer";

    public RefreshTokenResponse(String accessToken, String refreshToken, String messageRefreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.messageRefreshToken = messageRefreshToken;
    }
}
