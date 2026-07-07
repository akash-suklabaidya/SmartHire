package com.backend.smarthire.interceptor;

import com.backend.smarthire.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitingService rateLimitingService;

    public RateLimitInterceptor(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. Identify the user. We will use their IP address to track them.
        // (If you wanted to limit by user account, you could extract their JWT email here instead!)
        String ipAddress = request.getRemoteAddr();

        // 2. Fetch the bucket specifically assigned to this IP address
        Bucket tokenBucket = rateLimitingService.resolveBucket(ipAddress);

        // 3. Try to consume 1 token for this API request.
        // A "Probe" returns the result of the attempt.
        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // SUCCESS: Token was consumed.
            // We add a custom HTTP header to let the frontend know how many requests they have left!
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }
        else{
            // FAILURE: No tokens left! Block the request.
            // Calculate how many seconds until they get a new token.
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            // Add a header telling the frontend exactly when to try again
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));

            // Send a 429 TOO MANY REQUESTS status code directly back to Postman/Browser
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                    "You have exhausted your API Request Quota. Please wait " + waitForRefill + " seconds.");

            return false; // Return false to stop the request immediately
        }

    }

}