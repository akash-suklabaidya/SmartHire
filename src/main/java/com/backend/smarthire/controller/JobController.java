package com.backend.smarthire.controller;
import com.backend.smarthire.dto.ApiResponse;
import com.backend.smarthire.model.Job;
import com.backend.smarthire.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("NullableProblems")
@RestController // Tells Spring this handles incoming HTTP requests
@RequestMapping("/api/jobs") // All URLs in this class will start with /api/jobs
public class JobController {

    private final JobService jobService;

    // Spring gives us the repository we just made
    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // When someone sends a POST request to /api/jobs, run this method
    @SuppressWarnings("NullableProblems")
    @PostMapping
    public ResponseEntity<ApiResponse<Job>> createJob(@RequestBody Job job) {
        Job savedJob=jobService.createJob(job);
        return ResponseEntity.ok(new ApiResponse<>(true,"Job posted successfully!",savedJob));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Job>>> getAllJobs() {
        List<Job> jobs=jobService.getAllJobs();
        return ResponseEntity.ok(new ApiResponse<>(true, "Jobs fetched successfully!", jobs));
    }
}