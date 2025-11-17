package com.eventify.dto;

import lombok.Data;

@Data
public class UserRegistrationDto {
    private String name;
    private String email;
    private String password;
}