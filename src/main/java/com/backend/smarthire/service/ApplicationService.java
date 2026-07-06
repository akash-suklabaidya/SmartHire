package com.backend.smarthire.service;

import com.backend.smarthire.model.User;
import com.backend.smarthire.repository.ApplicationRepository;
import com.backend.smarthire.repository.JobRepository;
import com.backend.smarthire.repository.ProfileRepository;
import com.backend.smarthire.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

    public ApplicationService(ApplicationRepository applicationRepository, UserRepository userRepository, AiMatchmakerService aiMatchmakerService, JobRepository jobRepository, ProfileRepository profileRepository, SimpMessagingTemplate messagingTemplate) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.aiMatchmakerService = aiMatchmakerService;
        this.jobRepository = jobRepository;
        this.profileRepository = profileRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void applyForJob(Long jobId, String userEmail) {
        User candidate=userRepository.findByEmail(userEmail);
        if(candidate==null){
            throw new RuntimeException("User not found!");
        }
        // 1. Fetch necessary text data
        String jobDescription=jobRepository.getJobDescription(jobId);
        String resumeText=profileRepository.getResumeTextByUserId(candidate.getId());

        // Block the application completely if they haven't uploaded a resume yet
        if (resumeText == null || resumeText.trim().isEmpty()) {
            throw new RuntimeException("You must upload a resume to your profile before applying for jobs!");
        }
        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            throw new RuntimeException("Job description not found!");
        }

        // 2. Run the AI pipeline
        Double matchPercentage = aiMatchmakerService.calculateSingleCandidateMatch(jobDescription, candidate.getId());
        String aiSummary = aiMatchmakerService.generateMatchScore(jobDescription, resumeText);
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

    public void updateApplicationStatus(Long applicationId, String newStatus) {
        // 1. Update status in the database via Repository
        int updatedRows = applicationRepository.updateStatus(applicationId, newStatus);

        if(updatedRows==0){
            throw new RuntimeException("Application record not found.");
        }
        // 2. Fetch the candidate's user ID via Repository
        Long candidateId=applicationRepository.getCandidateIdByApplicationId(applicationId);

        if(candidateId==null){
            throw new RuntimeException("Candidate associated with this application not found.");
        }

        // 3. PUSH IT LIVE! Send a JSON payload down the open WebSocket tunnel
        Map<String, Object> updatePayload = Map.of(
                "applicationId", applicationId,
                "status", newStatus,
                "message", "Your application status has been updated to: " + newStatus
        );

        messagingTemplate.convertAndSend("/topic/updates/" + candidateId, (Object) updatePayload);
        System.out.println("📢 WEBSOCKET SENT: Pushed status update to /topic/updates/" + candidateId);

    }

}
