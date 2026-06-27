package com.backend.smarthire.repository;

import com.backend.smarthire.model.Job;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class JobRepository {

    private final JdbcTemplate jdbcTemplate;

    public JobRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    public void save(Job job){
        String sql="INSERT INTO jobs (title, description, created_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql,job.getTitle(),job.getDescription(), LocalDateTime.now());
        System.out.println("✅ SUCCESS: Job saved to Neon Database!");
    }

    public List<Job> findAll(){
        String sql = "SELECT * FROM jobs ORDER BY created_at DESC";
        return jdbcTemplate.query(sql,(rs,rowNum) -> {
            Job job = new Job();
            job.setId(rs.getLong("id"));
            job.setTitle(rs.getString("title"));
            job.setDescription(rs.getString("description"));
            job.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return job;
        });
    }

}
