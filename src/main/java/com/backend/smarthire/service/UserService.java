package com.backend.smarthire.service;

import com.backend.smarthire.model.User;
import com.backend.smarthire.repository.UserRepository;
import com.backend.smarthire.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.jwtUtil=jwtUtil;
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

    public String loginUser(User loginRequest){
        User existingUser=userRepository.findByEmail(loginRequest.getEmail());
        if (existingUser == null) {
            throw new RuntimeException("Error: User not found!");
        }
        boolean isPasswordMatch=passwordEncoder.matches(loginRequest.getPassword(),existingUser.getPassword());
        if (isPasswordMatch) {
            return jwtUtil.generateToken(existingUser.getEmail(),existingUser.getRole());
        } else {
            throw new RuntimeException("Error: Invalid password!");
        }
    }


}
