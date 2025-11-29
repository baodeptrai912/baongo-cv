package com.example.baoNgoCv.model.dto.user;

import lombok.Data;

@Data
public class PostForgetPasswordFinalRequest {
    private String username;
    private String newPassword;
    private String confirmPassword;
}
