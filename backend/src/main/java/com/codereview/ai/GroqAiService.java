package com.codereview.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroqAiService {

  @Value("${groq.api.key}")
  private String apiKey;

  @Value("${groq.api.url}")
  private String apiUrl;

  @Value("${groq.model}")
  private String model;

  private final ObjectMapper objectMapper;

  private final HttpClient httpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(30))
      .build();

  public String analyzeCode(String language, String code) {
    log.info("Sending code review request to Groq. Model: {}, Language: {}", model, language);
    return callGroqApi(buildPrompt(language, code));
  }

  public String refactorCode(String language, String code) {
    log.info("Sending refactor request to Groq. Language: {}", language);
    return callGroqApi(buildRefactorPrompt(language, code));
  }

  private String callGroqApi(String prompt) {
    try {
      Map<String, Object> body = Map.of(
          "model", model,
          "messages", List.of(
              Map.of("role", "user", "content", prompt)),
          "temperature", 0.3,
          "max_tokens", 2048);

      String jsonBody = objectMapper.writeValueAsString(body);

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(apiUrl))
          .timeout(Duration.ofSeconds(60))
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + apiKey)
          .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
          .build();

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      log.info("Groq response status: {}", response.statusCode());

      if (response.statusCode() != 200) {
        log.error("Groq API error {}: {}", response.statusCode(), response.body());
        throw new RuntimeException("Groq API returned " + response.statusCode() + ": " + response.body());
      }

      JsonNode root = objectMapper.readTree(response.body());
      String content = root.path("choices").get(0)
          .path("message").path("content").asText();

      log.info("Groq response received, length: {}", content.length());
      return content;

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      log.error("Groq API call failed: {}", e.getMessage(), e);
      throw new RuntimeException("AI service error: " + e.getMessage());
    }
  }

  private String buildPrompt(String language, String code) {
    return """
        You are a senior software engineer and code reviewer.

        Review the following %s code and respond ONLY with valid JSON — no markdown, no explanation, just the raw JSON object.

        Required JSON format:
        {
          "qualityScore": 75,
          "readabilityScore": 80,
          "securityScore": 90,
          "performanceScore": 70,
          "summary": "Brief 1-2 sentence summary of the code quality.",
          "issues": ["issue 1", "issue 2"],
          "suggestions": ["suggestion 1", "suggestion 2"],
          "securityWarnings": ["warning 1"],
          "performanceNotes": ["note 1"]
        }

        Code to review:
        %s
        """
        .formatted(language, code);
  }

  private String buildRefactorPrompt(String language, String code) {
    return """
        You are a senior software engineer. Refactor the following %s code.
        Return ONLY the refactored code. No explanation, no markdown fences.

        Code:
        %s
        """.formatted(language, code);
  }
}