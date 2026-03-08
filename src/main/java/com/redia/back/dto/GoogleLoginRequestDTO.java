package com.redia.back.dto;

public class GoogleLoginRequestDTO {

    private String token;

    public GoogleLoginRequestDTO() {
    }

    public GoogleLoginRequestDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
