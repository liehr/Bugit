package de.tudl.playground.bugit.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RequestThrottleFilter implements Filter {

    private static final long THRESHOLD_TIME_MS = TimeUnit.SECONDS.toMillis(20); // Zeitfenster (10s)
    private static final int MAX_REQUESTS = 20; // Maximale Anfragen pro IP

    private final ConcurrentHashMap<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String clientIp = httpRequest.getRemoteAddr();

            RequestInfo requestInfo = requestCounts.getOrDefault(clientIp, new RequestInfo(0, System.currentTimeMillis()));

            if (System.currentTimeMillis() - requestInfo.startTime > THRESHOLD_TIME_MS) {
                // Reset the count after a time window
                requestInfo = new RequestInfo(1, System.currentTimeMillis());
            } else {
                // Increment request count
                requestInfo.count++;
            }

            requestCounts.put(clientIp, requestInfo);

            if (requestInfo.count > MAX_REQUESTS) {
                httpResponse.setStatus(429);
                httpResponse.setHeader("Retry-After", "10");
                httpResponse.getWriter().write("Too many requests. Please slow down.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private static class RequestInfo {
        int count;
        long startTime;

        RequestInfo(int count, long startTime) {
            this.count = count;
            this.startTime = startTime;
        }
    }
}

