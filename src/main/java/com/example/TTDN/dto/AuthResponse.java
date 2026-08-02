package com.example.TTDN.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private String username;
    private String role;
}