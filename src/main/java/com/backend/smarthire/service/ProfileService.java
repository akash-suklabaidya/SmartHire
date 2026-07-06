package com.backend.smarthire.service;

import com.backend.smarthire.model.CandidateProfile;
import com.backend.smarthire.model.User;
import com.backend.smarthire.repository.ProfileRepository;
import com.backend.smarthire.repository.UserRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final EmbeddingModel embeddingModel;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository, EmbeddingModel embeddingModel) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.embeddingModel = embeddingModel;
    }

    public void upsertProfile(String email, CandidateProfile profileData) {
        User user=userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User account not found!");
        }
        profileData.setUserId(user.getId());
        profileRepository.saveOrUpdateProfile(profileData);
    }

    public String extractTextFromPdf(MultipartFile file) throws Exception {
        // 1. Validate file type
        if(!"application/pdf".equals(file.getContentType())){
            throw new IllegalArgumentException("Only PDF files are allowed.");
        }
        // 2. Extract text using PDFBox 3.0+ syntax        try(InputStream inputStream=file.getInputStream()){
        byte[] fileBytes=file.getBytes();
        try(PDDocument document= Loader.loadPDF(new RandomAccessReadBuffer(fileBytes))){
            PDFTextStripper pdfTextStripper=new PDFTextStripper();
            return pdfTextStripper.getText(document);
        }
    }

    // Parsing the local pdf files from system
    public String extractTextFromPdf(File file) throws Exception {
        // 1. Validate file extension
        if(!file.getName().toLowerCase().endsWith(".pdf")){
            throw new IllegalArgumentException("Only PDF files are allowed.");
        }
        // 2. Extract text using PDFBox 3.0+ syntax for java.io.File
        try(PDDocument document=Loader.loadPDF(file)){
            PDFTextStripper pdfTextStripper=new PDFTextStripper();
            return pdfTextStripper.getText(document);
        }
    }

    public void saveResumeTextForUser(String email, String resumeText) {
        // 1. Find the user
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found!");
        }
        // 2. Generate the Embedding!
        Response<Embedding> response = embeddingModel.embed(resumeText);
        float[] vectorArray = response.content().vector();

        // 3. Convert the float[] array into a String formatted as "[0.1, 0.2, ...]" for PostgreSQL
        String embeddingString = Arrays.toString(vectorArray);

        // 4. Save both the text and the vector to the database
        profileRepository.upsertResumeText(user.getId(), resumeText, embeddingString);
    }

    public String getResumeText(Long userId){
        return profileRepository.getResumeTextByUserId(userId);
    }

}
