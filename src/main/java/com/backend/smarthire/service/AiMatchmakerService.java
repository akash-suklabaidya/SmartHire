package com.backend.smarthire.service;

import com.backend.smarthire.model.CandidateMatch;
import com.backend.smarthire.repository.ProfileRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import java.util.Arrays;
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

    private final EmbeddingModel embeddingModel;
    private  final ProfileRepository profileRepository;

    public AiMatchmakerService(EmbeddingModel embeddingModel, ProfileRepository profileRepository) {
        this.embeddingModel = embeddingModel;
        this.profileRepository = profileRepository;
    }

    public List<CandidateMatch> matchCandidatesToJob(String jobDescription, int limit) {
        // 1. Send the Job Description to OpenAI to get the mathematical vector
        Response<Embedding> response=embeddingModel.embed(jobDescription);
        float[] vectorArray=response.content().vector();

        // 2. Convert the float[] into the String format PostgreSQL expects
        String jobVectorString = Arrays.toString(vectorArray);

        // 3. Pass the vector to our Repository method to execute the math
        return profileRepository.findSimilarCandidates(jobVectorString,limit);
    }

    public String generateMatchScore(String jobDescription, String resumeText) {
        // 1. Give the AI strict instructions on how to behave
        String systemPrompt = "You are an expert tech recruiter. Compare the job description to the resume. " +
                "Write a concise, one-sentence explanation of why the candidate is a fit, and note any missing skills. " +
                "CRITICAL: Do NOT generate or include any percentage numbers in your response. " +
                "Example: 'Strong experience in Spring Boot and React, but lacks explicit PostgreSQL experience.'";

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

    public Double calculateSingleCandidateMatch(String jobDescription, Long userId) {
        // Convert job description to a vector
        float[] vectorArray = embeddingModel.embed(jobDescription).content().vector();
        String jobVectorString = java.util.Arrays.toString(vectorArray);
        // Query the database for this specific user's match score
        return profileRepository.getSingleMatchPercentage(jobVectorString,userId);

    }

    public List<CandidateMatch> matchCandidatesToJobEmbedding(String jobEmbeddingString, int limit) {
        // Pass the pre-computed database vector string straight to our profile repository
        return profileRepository.findSimilarCandidates(jobEmbeddingString, limit);
    }

    public String generateInterviewQuestions(String jobDescription, String resumeText, String candidateName) {
        String systemPrompt = "You are an elite technical interviewer. Generate 3 highly customized interview questions for " + candidateName + ".\n" +
                "1. The first question should test a core skill they possess that matches the job.\n" +
                "2. The second question must directly target a gap or missing skill identified between their resume and the job description to test their adaptability.\n" +
                "3. The third question should be a behavioral scenario tailored to their project experience.\n" +
                "Keep the questions concise, professional, and direct. Do not include introductory text, just output the numbered questions.";

        String userPrompt = "Job Description:\n" + jobDescription + "\n\nCandidate Resume:\n" + resumeText;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));

        requestBody.put("temperature", 0.5);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, request, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            return "Error generating interview questions: " + e.getMessage();
        }

    }

    public String generateDraftEmail(String jobDescription, String resumeText, String candidateName, String emailType) {
        String behavior=emailType.equalsIgnoreCase("REJECTION")
                ? "a polite, professional, and empathetic rejection email. Gently mention a specific skill or requirement from the job description that was missing in their resume to provide closure, but encourage them to apply in the future."
                : "an enthusiastic and welcoming email inviting them to a first-round interview. Highlight a specific project or skill from their resume that perfectly matches the job description.";

        String systemPrompt = "You are an expert technical recruiter writing an email to a candidate named " + candidateName + ". " +
                "Write " + behavior + "\n" +
                "Keep it concise (under 150 words). Do not use generic placeholders like [Company Name] or [Your Name]—sign off simply as 'The SmartHire Recruiting Team'.";

        String userPrompt = "Job Description:\n" + jobDescription + "\n\nCandidate Resume:\n" + resumeText;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));

        requestBody.put("temperature", 0.6); // Slightly higher temperature for natural email tone
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, request, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            return "Error generating email: " + e.getMessage();
        }

    }
    public String askQuestionAboutResume(String resumeText, String question) {
        String systemPrompt = "You are a helpful HR assistant. Answer the user's question based ONLY on the provided resume text. " +
                "If the answer is not present in the resume, you must explicitly say: 'I cannot find this information in the resume.' " +
                "Do NOT hallucinate or guess.";
                
        String userPrompt = "Resume:\n" + resumeText + "\n\nQuestion:\n" + question;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        requestBody.put("temperature", 0.1); // Very low temperature to prevent hallucination

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, request, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            return "Error answering question: " + e.getMessage();
        }
    }

}
