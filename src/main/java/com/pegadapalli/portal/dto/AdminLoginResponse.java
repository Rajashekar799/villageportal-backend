package com.pegadapalli.portal.dto;

public class AdminLoginResponse {

    private String token;

    public AdminLoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
