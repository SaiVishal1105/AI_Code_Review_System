package com.codereview.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final ObjectMapper objectMapper;

    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of(
                "apiUrl", apiUrl,
                "model", model,
                "keyLoaded", apiKey != null && !apiKey.isBlank() ? "YES" : "NO",
                "keyPreview", apiKey != null && apiKey.length() > 10
                        ? apiKey.substring(0, 8) + "..." + apiKey.substring(apiKey.length() - 4)
                        : "MISSING");
    }

    @GetMapping("/groq")
    public Map<String, Object> testGroq() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(Map.of("role", "user", "content", "Say: OK")),
                    "max_tokens", 10);
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String content = root.path("choices").get(0)
                        .path("message").path("content").asText();
                return Map.of("status", "SUCCESS", "httpCode", response.statusCode(), "aiResponse", content);
            } else {
                return Map.of("status", "FAILED", "httpCode", response.statusCode(), "errorBody", response.body());
            }
        } catch (Exception e) {
            return Map.of("status", "EXCEPTION", "error", e.getClass().getSimpleName(), "message", e.getMessage());
        }
    }

    @GetMapping("/key")
    public Map<String, String> checkKey() {
        return Map.of(
                "keyPreview", apiKey != null && apiKey.length() > 10
                        ? apiKey.substring(0, 8) + "..." + apiKey.substring(apiKey.length() - 4)
                        : "MISSING OR TOO SHORT",
                "keyLength", String.valueOf(apiKey != null ? apiKey.length() : 0));
    }
}