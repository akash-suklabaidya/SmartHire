package com.backend.smarthire.service;

import com.backend.smarthire.model.CandidateProfile;
import com.backend.smarthire.model.User;
import com.backend.smarthire.repository.ProfileRepository;
import com.backend.smarthire.repository.UserRepository;
import org.springframework.stereotype.Service;

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

}
