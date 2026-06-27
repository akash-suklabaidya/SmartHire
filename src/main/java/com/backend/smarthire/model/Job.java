package com.backend.smarthire.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Job {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
}