package com.backend.smarthire.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ResumeEventConsumer {

    private final ProfileService profileService;

    public ResumeEventConsumer(ProfileService profileService) {
        this.profileService = profileService;
    }

    // This listens to the Kafka server running in your background terminal
    @KafkaListener(topics = "resume-processing-topic", groupId = "smarthire-ai-group")
    public void consumeResumeEvent(String message) {
        try {
            System.out.println("📥 KAFKA CONSUMER: Picked up message -> " + message);
            // The message is just the userEmail now
            String userEmail = message;

            // 2. Execute the embedding generation in the background
            profileService.generateAndSaveEmbedding(userEmail);

        } catch (Exception e) {
            System.err.println("❌ KAFKA CONSUMER ERROR: Failed to process resume in background.");
            e.printStackTrace();
        }
    }

}
