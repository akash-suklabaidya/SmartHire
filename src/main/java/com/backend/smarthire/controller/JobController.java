package com.backend.smarthire.controller;
import com.backend.smarthire.dto.ApiResponse;
import com.backend.smarthire.model.Job;
import com.backend.smarthire.service.ApplicationService;
import com.backend.smarthire.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@SuppressWarnings("NullableProblems")
@RestController // Tells Spring this handles incoming HTTP requests
@RequestMapping("/api/jobs") // All URLs in this class will start with /api/jobs
public class JobController {

    private final JobService jobService;
    private final ApplicationService applicationService;

    // Spring gives us the repository we just made
    public JobController(JobService jobService, ApplicationService applicationService) {
        this.jobService = jobService;
        this.applicationService=applicationService;
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

    @PostMapping("/{jobId}/apply")
    @PreAuthorize("hasRole('CANDIDATE')") // ONLY Candidates can apply!
    public ResponseEntity<ApiResponse<String>> applyForJob(@PathVariable Long jobId) {
        // 1. Extract the currently logged-in user's email directly from the JWT Context
        String currentUserEmail= SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            // 2. Process the application
            applicationService.applyForJob(jobId, currentUserEmail);
            return ResponseEntity.ok(new ApiResponse<>(true, "Successfully applied for the job!", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{jobId}/applications")
    @PreAuthorize("hasRole('RECRUITER')") // ONLY Recruiters can view applicants!
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getJobApplicants(@PathVariable Long jobId) {
        List<Map<String, Object>> applicants = applicationService.getApplicants(jobId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Applicants fetched successfully!", applicants));
    }


}