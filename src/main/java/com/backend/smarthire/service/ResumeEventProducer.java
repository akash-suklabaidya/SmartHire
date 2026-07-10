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

    public void sendResumeProcessingEvent(String userEmail) {
        // The "Topic" is the specific queue name we are sending this to
        String topic = "resume-processing-topic";

        // Fire it into Kafka!
        kafkaTemplate.send(topic, userEmail);

        System.out.println("✅ KAFKA EVENT FIRED: Sent embedding generation task to queue for User " + userEmail);

    }

}
