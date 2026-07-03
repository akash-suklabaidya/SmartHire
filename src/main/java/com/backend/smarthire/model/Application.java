package com.backend.smarthire.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Application {

    private Long id;
    private Long jobId;
    private Long userId;
    private LocalDateTime appliedAt;
    private Double matchPercentage;
    private String aiSummary;

}
