package com.backend.smarthire.controller;

import com.backend.smarthire.dto.ApiResponse;
import com.backend.smarthire.model.CandidateProfile;
import com.backend.smarthire.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@SuppressWarnings("NullableProblems")
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
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

    @PostMapping("/upload-resume")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please select a file to upload."));
        }
        try{
            // Extract the text from the PDF
            String extractedText = profileService.extractTextFromPdf(file);
            // 2. Get the logged-in user's email
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            // 3. Save the text to the database
            profileService.saveResumeTextForUser(currentUserEmail, extractedText);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "PDF parsed successfully!",
                    "preview", extractedText.substring(0, Math.min(extractedText.length(), 100)) + "..."
            ));
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to parse PDF: " + e.getMessage()));
        }
    }
}
