package com.backend.smarthire.service;

import com.backend.smarthire.model.Job;
import com.backend.smarthire.repository.JobRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
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

    public Job createJob(Job job) {
        // 1. Generate the vector embedding from the job description text
        Response<Embedding> response=embeddingModel.embed(job.getDescription());
        float[] vectorArray = response.content().vector();
        String embeddingString = Arrays.toString(vectorArray);
        // 2. Save both text and the embedding string to the database
        return jobRepository.save(job,embeddingString);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public String getJobEmbedding(Long jobId) {
        return jobRepository.getJobEmbedding(jobId);
    }

    public String getJobDescription(Long jobId) {
        return jobRepository.getJobDescription(jobId);
    }

}
