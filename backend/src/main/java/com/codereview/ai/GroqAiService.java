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
    log.info("Analyzing code. Language: {}, Model: {}", language, model);
    return callGroqApi(buildPrompt(language, code));
  }

  public String refactorCode(String language, String code) {
    log.info("Refactoring code. Language: {}", language);
    return callGroqApi(buildRefactorPrompt(language, code));
  }

  private String callGroqApi(String prompt) {
    try {
      Map<String, Object> body = Map.of(
          "model", model,
          "messages", List.of(Map.of("role", "user", "content", prompt)),
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

      log.info("Groq status: {}", response.statusCode());

      if (response.statusCode() != 200) {
        log.error("Groq error {}: {}", response.statusCode(), response.body());
        throw new RuntimeException("Groq returned " + response.statusCode() + ": " + response.body());
      }

      JsonNode root = objectMapper.readTree(response.body());
      String content = root.path("choices").get(0)
          .path("message").path("content").asText();

      log.info("Groq response OK, length: {}", content.length());
      return content;

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      log.error("Groq call failed: {}", e.getMessage(), e);
      throw new RuntimeException("AI service error: " + e.getMessage());
    }
  }

  private String buildPrompt(String language, String code) {
    return "You are a senior software engineer.\n\n" +
        "Review this " + language + " code. Reply ONLY with a valid JSON object, no markdown, no extra text.\n\n" +
        "JSON format:\n" +
        "{\n" +
        "  \"qualityScore\": 75,\n" +
        "  \"readabilityScore\": 80,\n" +
        "  \"securityScore\": 90,\n" +
        "  \"performanceScore\": 70,\n" +
        "  \"summary\": \"Short summary here.\",\n" +
        "  \"issues\": [\"issue 1\", \"issue 2\"],\n" +
        "  \"suggestions\": [\"suggestion 1\"],\n" +
        "  \"securityWarnings\": [\"warning 1\"],\n" +
        "  \"performanceNotes\": [\"note 1\"]\n" +
        "}\n\n" +
        "Code:\n" + code;
  }

  private String buildRefactorPrompt(String language, String code) {
    return "You are a senior software engineer. Refactor this " + language + " code.\n" +
        "Return ONLY the refactored code. No markdown, no explanation.\n\n" +
        "Code:\n" + code;
  }
}