package com.backend.smarthire.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String email;
    private String password;

    // This will hold either "RECRUITER" or "CANDIDATE"
    private String role;

    private LocalDateTime createdAt;
}
