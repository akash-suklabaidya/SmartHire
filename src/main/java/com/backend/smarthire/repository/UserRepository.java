package com.backend.smarthire.repository;

import com.backend.smarthire.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public void save(User user) {
        String sql = "INSERT INTO users (email, password, role, created_at) VALUES (?, ?, ?, ?)";

        jdbcTemplate.update(sql, user.getEmail(), user.getPassword(), user.getRole(), LocalDateTime.now());

        System.out.println("✅ SUCCESS: " + user.getRole() + " saved to Neon Database!");
    }
}
