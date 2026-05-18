package com.codereview.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    public ParsedReview parse(String aiResponse) {
        try {
            // Strip markdown fences if present
            String cleaned = aiResponse.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            }
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            JsonNode root = objectMapper.readTree(cleaned);

            ParsedReview result = new ParsedReview();
            result.setQualityScore(getInt(root, "qualityScore", 50));
            result.setReadabilityScore(getInt(root, "readabilityScore", 50));
            result.setSecurityScore(getInt(root, "securityScore", 50));
            result.setPerformanceScore(getInt(root, "performanceScore", 50));
            result.setSummary(root.path("summary").asText("No summary available."));
            result.setIssues(getList(root, "issues"));
            result.setSuggestions(getList(root, "suggestions"));
            result.setSecurityWarnings(getList(root, "securityWarnings"));
            result.setPerformanceNotes(getList(root, "performanceNotes"));

            return result;

        } catch (Exception e) {
            log.warn("Failed to parse AI JSON response, using fallback. Error: {}", e.getMessage());
            return buildFallback(aiResponse);
        }
    }

    private int getInt(JsonNode node, String field, int defaultVal) {
        JsonNode n = node.path(field);
        return n.isMissingNode() ? defaultVal : Math.min(100, Math.max(0, n.asInt(defaultVal)));
    }

    private List<String> getList(JsonNode root, String field) {
        List<String> items = new ArrayList<>();
        JsonNode arr = root.path(field);
        if (arr.isArray()) {
            arr.forEach(item -> items.add(item.asText()));
        }
        return items;
    }

    private ParsedReview buildFallback(String rawText) {
        ParsedReview fallback = new ParsedReview();
        fallback.setQualityScore(60);
        fallback.setReadabilityScore(60);
        fallback.setSecurityScore(60);
        fallback.setPerformanceScore(60);
        fallback.setSummary("AI review completed. See raw analysis below.");
        fallback.setIssues(List.of("Review parsing failed — see summary for details", rawText));
        fallback.setSuggestions(List.of("Re-submit for a structured review"));
        fallback.setSecurityWarnings(List.of());
        fallback.setPerformanceNotes(List.of());
        return fallback;
    }

    @lombok.Data
    public static class ParsedReview {
        private int qualityScore;
        private int readabilityScore;
        private int securityScore;
        private int performanceScore;
        private String summary;
        private List<String> issues;
        private List<String> suggestions;
        private List<String> securityWarnings;
        private List<String> performanceNotes;
    }
}
