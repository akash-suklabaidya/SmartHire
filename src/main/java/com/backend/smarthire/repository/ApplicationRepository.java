package com.backend.smarthire.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ApplicationRepository {

    private final JdbcTemplate jdbcTemplate;

    public ApplicationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveApplication(Long jobId, Long userId){
        String sql="INSERT INTO applications (job_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql,jobId,userId);
    }

    public List<Map<String, Object>> getApplicantsForJob(Long jobId) {
        String sql = "SELECT u.email, a.applied_at, " +
                "cp.headline, cp.skills, cp.resume_text " +
                "FROM users u " +
                "JOIN applications a ON u.id = a.user_id " +
                "LEFT JOIN candidate_profiles cp ON u.id = cp.user_id " +
                "WHERE a.job_id = ?";
        return jdbcTemplate.queryForList(sql,jobId);
    }



}
