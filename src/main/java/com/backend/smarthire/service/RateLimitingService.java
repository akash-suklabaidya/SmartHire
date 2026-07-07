package com.backend.smarthire.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {
    // We store each user's "bucket" in a map, keyed by their IP address or Email.
    // NOTE: In a multi-server setup (like deploying 5 instances on AWS),
    // this map is replaced by the RedisProxyManager to sync buckets across servers.

    private final Map<String, Bucket> cache=new ConcurrentHashMap<>();
    public Bucket resolveBucket(String key) {
        // computeIfAbsent checks if the user's IP already has a bucket.
        // If they do, it returns it. If they don't, it triggers newBucket() to create one.
        return cache.computeIfAbsent(key, this::newBucket);
    }

    private Bucket newBucket(String key){
        // -------------------------------------------------------------------------
        // BUCKET RULES:
        // 1. Capacity: 5 tokens (Maximum burst of 5 requests allowed at once)
        // 2. Refill: Adds 5 tokens back every 1 minute
        // This means a user can make 5 rapid requests, but then must wait for a refill.
        // -------------------------------------------------------------------------
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

}
