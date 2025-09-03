package com.example.experfolio.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Application health check endpoints")
public class HealthController {

    @GetMapping
    @Operation(
            summary = "Health Check",
            description = "Returns the health status of the application"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application is healthy"),
            @ApiResponse(responseCode = "503", description = "Application is unhealthy")
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Experfolio API");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detailed")
    @Operation(
            summary = "Detailed Health Check",
            description = "Returns detailed health information including system status"
    )
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> system = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        system.put("totalMemory", totalMemory / 1024 / 1024 + " MB");
        system.put("freeMemory", freeMemory / 1024 / 1024 + " MB");
        system.put("usedMemory", usedMemory / 1024 / 1024 + " MB");
        system.put("availableProcessors", runtime.availableProcessors());
        
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Experfolio API");
        response.put("version", "1.0.0");
        response.put("system", system);
        
        return ResponseEntity.ok(response);
    }
}