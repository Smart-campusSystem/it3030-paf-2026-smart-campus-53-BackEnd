package com.smart_campus_system.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check endpoint for monitoring and load balancer health probes.
 * Accessible without authentication (must be permitted in SecurityConfig).
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        response.put("service", "smart-campus-backend");

        // Database check
        try (Connection conn = dataSource.getConnection()) {
            response.put("database", conn.isValid(2) ? "UP" : "DOWN");
        } catch (Exception e) {
            response.put("database", "DOWN");
            response.put("database_error", e.getMessage());
        }

        // Redis check
        try {
            String pong = redisConnectionFactory.getConnection().ping();
            response.put("redis", pong != null ? "UP" : "DOWN");
        } catch (Exception e) {
            response.put("redis", "DOWN");
            response.put("redis_error", e.getMessage());
        }

        boolean allUp = "UP".equals(response.get("database")) && "UP".equals(response.get("redis"));
        response.put("status", allUp ? "UP" : "DEGRADED");

        return allUp
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(503).body(response);
    }
}
