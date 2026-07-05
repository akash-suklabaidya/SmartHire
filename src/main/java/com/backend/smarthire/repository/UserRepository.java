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
    public User save(User user) {
        // Add "RETURNING id" to tell PostgreSQL to hand back the new Primary Key instantly
        String sql = "INSERT INTO users (email, password, role, created_at) VALUES (?, ?, ?, ?) RETURNING id";

        // Use queryForObject instead of update so we can capture that returned ID
        Long generatedId = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                LocalDateTime.now()
        );

        user.setId(generatedId);

        System.out.println("✅ SUCCESS: " + user.getRole() + " saved to Neon Database with ID: " + generatedId);

        return user;
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


