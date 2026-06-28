package com.backend.smarthire.service;

import com.backend.smarthire.model.User;
import com.backend.smarthire.repository.ApplicationRepository;
import com.backend.smarthire.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public ApplicationService(ApplicationRepository applicationRepository, UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
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
        return applicationRepository.getApplicantsForJob(jobId);
    }

}
