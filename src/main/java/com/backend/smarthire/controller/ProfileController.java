package com.backend.smarthire.controller;

import com.backend.smarthire.dto.ApiResponse;
import com.backend.smarthire.model.CandidateProfile;
import com.backend.smarthire.service.AiMatchmakerService;
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
    private final AiMatchmakerService aiMatchmakerService;

    public ProfileController(ProfileService profileService, ResumeEventProducer resumeEventProducer, AiMatchmakerService aiMatchmakerService) {
        this.profileService = profileService;
        this.resumeEventProducer = resumeEventProducer;
        this.aiMatchmakerService = aiMatchmakerService;
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

            // 2. Extract text synchronously (takes milliseconds)
            String extractedText = profileService.extractTextFromPdf(file);
            
            if (extractedText == null || extractedText.trim().isEmpty()) {
                 return ResponseEntity.badRequest().body(Map.of("error", "We couldn't extract any text from this PDF. Please ensure it is a text-based PDF and not an image."));
            }

            // 3. Save text to DB and trigger Kafka for embedding
            profileService.saveResumeTextAndTriggerEmbedding(currentUserEmail, extractedText);

            // 4. Instantly return success to the frontend
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

    @PostMapping("/{candidateId}/ask")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<String>> askQuestionAboutResume(
            @PathVariable Long candidateId,
            @RequestBody Map<String, String> payload) {
        
        String question = payload.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Question is required", null));
        }

        // 1. Fetch the resume text from the DB
        String resumeText = profileService.getResumeText(candidateId);
        if (resumeText == null || resumeText.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Candidate does not have a parsed resume on file.", null));
        }

        // 2. Ask the AI
        String answer = aiMatchmakerService.askQuestionAboutResume(resumeText, question);
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", answer));
    }
}
