package com.backend.smarthire.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CandidateProfile {
    private Long id;
    private Long userId;
    private String headline;
    private String skills;
    private String resumeText;
    private LocalDateTime updatedAt;

}
