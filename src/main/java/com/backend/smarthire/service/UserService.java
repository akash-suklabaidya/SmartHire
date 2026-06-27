package com.backend.smarthire.service;

import com.backend.smarthire.model.User;
import com.backend.smarthire.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder=passwordEncoder;
    }

    public User registerUser(User user) {
        String plainTextPassword=user.getPassword();
        String hashedPassword=passwordEncoder.encode(plainTextPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
        return user;
    }

}
