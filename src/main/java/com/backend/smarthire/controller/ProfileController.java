package com.backend.smarthire.controller;

import com.backend.smarthire.dto.ApiResponse;
import com.backend.smarthire.model.CandidateProfile;
import com.backend.smarthire.service.ProfileService;
import com.backend.smarthire.service.ResumeEventProducer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

@SuppressWarnings("NullableProblems")
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;
    private final ResumeEventProducer resumeEventProducer;

    public ProfileController(ProfileService profileService, ResumeEventProducer resumeEventProducer) {
        this.profileService = profileService;
        this.resumeEventProducer = resumeEventProducer;
    }

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')") // Only Candidates can have/update profiles
    public ResponseEntity<ApiResponse<String>> upsertCandidateProfile(@RequestBody CandidateProfile profileData) {

        String currentUserEmail= SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            profileService.upsertProfile(currentUserEmail, profileData);
            return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully!", null));
        }
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }

    }

    @PostMapping(value = "/upload-resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please select a file to upload."));
        }
        try{
            // 1. Get the logged-in user's email
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

            // 2. Create a temporary file on your local machine to safely store the PDF
            String tempDir=System.getProperty("java.io.tmpdir");
            File tempFile = new File(tempDir, System.currentTimeMillis() + "_" + file.getOriginalFilename());

            // 3. Physically save the uploaded file to that location
            file.transferTo(tempFile);

            // 4. Send the file path to Kafka! (Takes milliseconds)
            resumeEventProducer.sendResumeProcessingEvent(currentUserEmail, tempFile.getAbsolutePath());

            // 5. Instantly return success to the frontend
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Resume received! The AI is reading it in the background."
            ));


        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to parse PDF: " + e.getMessage()));
        }
    }
}
