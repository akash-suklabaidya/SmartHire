package com.backend.smarthire.model;

import lombok.Data;

@Data
public class CandidateMatch {
    private Long userId;
    private String headline;
    private String resumeTextPreview;
    private double matchPercentage;
    private String fullResumeText;
    private String aiSummary;
}
