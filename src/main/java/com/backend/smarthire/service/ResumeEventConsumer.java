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
            // 1. Unpack the event message (Email || FilePath)
            String[] parts = message.split("\\|\\|");
            String userEmail = parts[0];
            String filePath = parts[1];

            File pdfFile = new File(filePath);

            // 2. Execute your text extraction and DB save in the background
            String extractedText=profileService.extractTextFromPdf(pdfFile);
            profileService.saveResumeTextForUser(userEmail,extractedText);

            System.out.println("✅ BACKGROUND TASK COMPLETE: Saved resume text to DB for " + userEmail);

            // 3. Delete the temporary file so it doesn't clutter your hard drive
            if(pdfFile.exists()){
                pdfFile.delete();
            }

        } catch (Exception e) {
            System.err.println("❌ KAFKA CONSUMER ERROR: Failed to process resume in background.");
            e.printStackTrace();
        }
    }

}
