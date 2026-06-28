package com.backend.smarthire.controller;

import com.backend.smarthire.dto.ApiResponse;
import com.backend.smarthire.model.CandidateProfile;
import com.backend.smarthire.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
