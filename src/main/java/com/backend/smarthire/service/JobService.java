package com.backend.smarthire.service;

import com.backend.smarthire.model.Job;
import com.backend.smarthire.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class JobService {
    private final JobRepository jobRepository;
    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Job createJob(Job job) {
        jobRepository.save(job);
        return job;
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }
}
