package com.example.TTDN.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String role; // "ROLE_STUDENT", "ROLE_TEACHER", hoặc "ROLE_ADMIN"
}