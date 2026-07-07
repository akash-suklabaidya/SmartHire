package com.backend.smarthire.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AdminRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // 1. Total Jobs Posted
        Integer totalJobs = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM jobs", Integer.class);
        metrics.put("totalJobs", totalJobs != null ? totalJobs : 0);

        // 2. Total Candidates Registered
        Integer totalCandidates = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE role = 'CANDIDATE'", Integer.class);
        metrics.put("totalCandidates", totalCandidates != null ? totalCandidates : 0);

        // 3. Total Applications Submitted
        Integer totalApplications = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM applications", Integer.class);
        metrics.put("totalApplications", totalApplications != null ? totalApplications : 0);

        // 4. Application Status Breakdown (How many are hired, rejected, etc.)
        String statusSql = "SELECT status, COUNT(*) as count FROM applications GROUP BY status";
        List<Map<String, Object>> statusBreakdown = jdbcTemplate.queryForList(statusSql);
        metrics.put("applicationStatuses", statusBreakdown);

        return metrics;
    }
}