package com.backend.smarthire.service;

import com.backend.smarthire.model.CandidateProfile;
import com.backend.smarthire.model.User;
import com.backend.smarthire.repository.ProfileRepository;
import com.backend.smarthire.repository.UserRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
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
    public void saveResumeTextForUser(String email, String resumeText) {
        // 1. Find the user
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found!");
        }
        // 2. Delegate the database save to the correct repository
        profileRepository.upsertResumeText(user.getId(), resumeText);
    }

}
