package com.backend.smarthire.service;

import com.backend.smarthire.model.Job;
import com.backend.smarthire.repository.JobRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
@Service
public class JobService {
    private final JobRepository jobRepository;
    private final EmbeddingModel embeddingModel;
    public JobService(JobRepository jobRepository, EmbeddingModel embeddingModel) {
        this.jobRepository = jobRepository;
        this.embeddingModel = embeddingModel;
    }

    // -------------------------------------------------------------------------
    // @CacheEvict:
    // When a recruiter creates a new job, our cached list becomes outdated.
    // This annotation tells Redis to delete the "jobs" cache entirely.
    // The NEXT time a user calls getAllJobs(), it will fetch fresh data from the DB.
    // -------------------------------------------------------------------------
    @CacheEvict(value = "jobs", allEntries = true)
    public Job createJob(Job job) {
        // 1. Generate the vector embedding from the job description text
        Response<Embedding> response=embeddingModel.embed(job.getDescription());
        float[] vectorArray = response.content().vector();
        String embeddingString = Arrays.toString(vectorArray);
        // 2. Save both text and the embedding string to the database
        System.out.println("CACHE CLEARED: A new job was posted.");
        return jobRepository.save(job,embeddingString);
    }

    // -------------------------------------------------------------------------
    // @Cacheable:
    // 1. Spring intercepts this method call.
    // 2. It checks Redis for a key named "allJobs".
    // 3. If found, it skips the SQL query and returns the Redis data instantly.
    // 4. If NOT found, it runs the SQL query, saves the result to Redis, and returns it.
    // -------------------------------------------------------------------------
    @Cacheable(value = "jobs", key = "'allJobs'")
    public List<Job> getAllJobs() {
        System.out.println("CACHE MISS: Hitting the PostgreSQL Database for Jobs...");
        return jobRepository.findAll();
    }

    public String getJobEmbedding(Long jobId) {
        return jobRepository.getJobEmbedding(jobId);
    }

    public String getJobDescription(Long jobId) {
        return jobRepository.getJobDescription(jobId);
    }



}
