package com.backend.smarthire.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ResumeEventProducer {
    // KafkaTemplate is a built-in Spring Boot tool that handles all the complex network connections
    private final KafkaTemplate<String,String> kafkaTemplate;

    public ResumeEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendResumeProcessingEvent(String userEmail, String filePath) {
        // The "Topic" is the specific queue name we are sending this to
        String topic = "resume-processing-topic";

        // The message we are sending (a simple string combining the user ID and where the file is saved)
        String message = userEmail + "||" + filePath;

        // Fire it into Kafka!
        kafkaTemplate.send(topic,message);

        System.out.println("✅ KAFKA EVENT FIRED: Sent file " + filePath + " to queue for User " + userEmail);

    }

}
