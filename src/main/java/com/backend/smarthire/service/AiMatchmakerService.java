package com.backend.smarthire.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiMatchmakerService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private final RestTemplate restTemplate=new RestTemplate();

    public String generateMatchScore(String jobDescription, String resumeText) {
        // 1. Give the AI strict instructions on how to behave
        String systemPrompt = "You are an expert tech recruiter. Compare the job description to the resume. " +
                "Return ONLY a percentage match followed by a one-sentence explanation. " +
                "Example: '85% Match: Strong Java skills, but lacks Kafka experience.'";

        String userPrompt = "Job Description:\n" + jobDescription + "\n\nResume:\n" + resumeText;
        // 2. Set up the headers with your secure API key
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 3. Build the JSON payload exactly how OpenAI expects it
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model",model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        requestBody.put("temperature", 0.3); // Low temperature means more analytical, less creative
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try{
            // 4. Send the request to OpenAI
            ResponseEntity<Map> response=restTemplate.postForEntity(OPENAI_URL,request,Map.class);
            // 5. Extract the text from the response JSON
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        }
        catch (Exception e){
            return "Error calculating match score: " + e.getMessage();
        }

    }


}
