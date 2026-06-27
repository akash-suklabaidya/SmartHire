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

    public User findByEmail(String email){
        String sql="SELECT * FROM users WHERE email = ?";
        try{
            // queryForObject is used when we expect exactly ONE result back (not a List)
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                return user;
            }, email); // The email fills in the '?'
        }
        catch (Exception e){
            return null;
        }

    }

}


