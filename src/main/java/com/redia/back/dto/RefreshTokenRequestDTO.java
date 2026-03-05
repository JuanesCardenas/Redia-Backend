package com.redia.back.dto;

/**
 * DTO para solicitar nuevo access token usando refresh token.
 */
public class RefreshTokenRequestDTO {

    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}