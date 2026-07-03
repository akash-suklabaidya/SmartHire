package com.backend.smarthire.service;

import com.backend.smarthire.model.User;
import com.backend.smarthire.repository.ApplicationRepository;
import com.backend.smarthire.repository.JobRepository;
import com.backend.smarthire.repository.ProfileRepository;
import com.backend.smarthire.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final AiMatchmakerService aiMatchmakerService;
    private final JobRepository jobRepository;
    private final ProfileRepository profileRepository;

    public ApplicationService(ApplicationRepository applicationRepository, UserRepository userRepository, AiMatchmakerService aiMatchmakerService, JobRepository jobRepository, ProfileRepository profileRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.aiMatchmakerService = aiMatchmakerService;
        this.jobRepository = jobRepository;
        this.profileRepository = profileRepository;
    }

    public void applyForJob(Long jobId, String userEmail) {
        User candidate=userRepository.findByEmail(userEmail);
        if(candidate==null){
            throw new RuntimeException("User not found!");
        }
        // 1. Fetch necessary text data
        String jobDescription=jobRepository.getJobDescription(jobId);
        String resumeText=profileRepository.getResumeTextByUserId(candidate.getId());

        Double matchPercentage = 0.0;
        String aiSummary = "No resume uploaded.";
        // 2. If they have a resume, run the AI pipeline!
        if(resumeText!=null && !resumeText.trim().isEmpty() && jobDescription!=null){
            matchPercentage=aiMatchmakerService.calculateSingleCandidateMatch(jobDescription,candidate.getId());
            aiSummary = aiMatchmakerService.generateMatchScore(jobDescription, resumeText);
        }
        // 3. Save everything to the database at once
        try {
            applicationRepository.saveApplication(jobId, candidate.getId(),matchPercentage,aiSummary);
        } catch (Exception e) {
            throw new RuntimeException("You have already applied for this job!");
        }
    }

    public List<Map<String, Object>> getApplicants(Long jobId) {
        // Because the AI math and summary were saved to the DB when the candidate applied,
        // we no longer need to call OpenAI here. We just fetch the data instantly!
        return applicationRepository.getApplicantsForJob(jobId);
    }

}
