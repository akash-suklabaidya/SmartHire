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

    public void saveApplication(Long jobId, Long userId, Double matchPercentage, String aiSummary) {
        String sql = "INSERT INTO applications (job_id, user_id, match_percentage, ai_summary) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, jobId, userId, matchPercentage, aiSummary);
    }

    public List<Map<String, Object>> getApplicantsForJob(Long jobId) {
        String sql = "SELECT u.id as user_id, u.email, " +
                "a.applied_at, a.match_percentage, a.ai_summary " +
                "FROM applications a " +
                "JOIN users u ON a.user_id = u.id " +
                "WHERE a.job_id = ? " +
                "ORDER BY a.match_percentage DESC NULLS LAST";

        return jdbcTemplate.queryForList(sql, jobId);
    }



}
