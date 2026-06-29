package com.backend.smarthire.service;

import com.backend.smarthire.model.User;
import com.backend.smarthire.repository.ApplicationRepository;
import com.backend.smarthire.repository.JobRepository;
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


    public ApplicationService(ApplicationRepository applicationRepository, UserRepository userRepository, AiMatchmakerService aiMatchmakerService, JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.aiMatchmakerService = aiMatchmakerService;
        this.jobRepository = jobRepository;
    }

    public void applyForJob(Long jobId, String userEmail) {
        User candidate=userRepository.findByEmail(userEmail);
        if(candidate==null){
            throw new RuntimeException("User not found!");
        }
        try {
            applicationRepository.saveApplication(jobId, candidate.getId());
        } catch (Exception e) {
            throw new RuntimeException("You have already applied for this job!");
        }
    }

    public List<Map<String, Object>> getApplicants(Long jobId) {

        // 1. Fetch raw applicants from the DB
        List<Map<String, Object>> rawApplicants = applicationRepository.getApplicantsForJob(jobId);

        // 2. Fetch the job description using the proper repository
        String jobDescription = jobRepository.getJobDescription(jobId);

        // 3. Create a new list to hold the updated data
        List<Map<String, Object>> enrichedApplicants = new ArrayList<>();

        for (Map<String, Object> applicant : rawApplicants) {
            // Make the map editable
            Map<String, Object> editableApplicant = new java.util.HashMap<>(applicant);
            String resumeText = (String) editableApplicant.get("resume_text");
            // 4. If they have a resume, ask the AI for a score!
            if (resumeText != null && !resumeText.trim().isEmpty() && !jobDescription.isEmpty()) {
                String aiResponse=aiMatchmakerService.generateMatchScore(jobDescription,resumeText);
                editableApplicant.put("aiMatchAnalysis", aiResponse);
            }
            else{
                editableApplicant.put("aiMatchAnalysis", "No profile data provided.");
            }
            enrichedApplicants.add(editableApplicant);
        }
        return enrichedApplicants;
    }

}
