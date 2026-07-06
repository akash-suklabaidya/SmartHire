package com.backend.smarthire.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Job implements Serializable {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
}