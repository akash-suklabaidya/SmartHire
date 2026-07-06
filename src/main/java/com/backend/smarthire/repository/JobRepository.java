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

    public Job save(Job job, String embeddingString){
        String sql = "INSERT INTO jobs (title, description, job_embedding, created_at) VALUES (?, ?, ?::vector, ?) RETURNING id";
        Long generatedId=jdbcTemplate.queryForObject(
                sql,
                Long.class,
                job.getTitle(),
                job.getDescription(),
                embeddingString,
                LocalDateTime.now()
        );
        job.setId(generatedId);
        job.setCreatedAt(LocalDateTime.now());
        System.out.println("✅ SUCCESS: Job saved to Neon Database with ID: " + generatedId);
        return job;
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

    public String getJobDescription(Long jobId) {
        String sql="SELECT description FROM jobs WHERE id = ?";
        try{
            return jdbcTemplate.queryForObject(sql, String.class, jobId);
        }
        catch (Exception e){
            return ""; // Return empty string if job description is missing
        }
    }

    public String getJobEmbedding(Long jobId) {
        String sql = "SELECT job_embedding::text FROM jobs WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, jobId);
        } catch (Exception e) {
            return null;
        }
    }

}
