package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("access_token")
    private String accessToken;
    
    @SerializedName("error_description")
    private String errorDescription;

    public String getAccessToken() {
        return accessToken;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}