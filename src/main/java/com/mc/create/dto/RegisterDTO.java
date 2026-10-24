package com.mc.create.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String confirmPassword;
}
