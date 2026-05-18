package com.codereview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ===================== REQUEST DTOs =====================

public class ReviewDto {

    @Data
    public static class ReviewRequest {
        @NotBlank(message = "Language is required")
        private String language;

        @NotBlank(message = "Code is required")
        @Size(max = 50000, message = "Code must be under 50,000 characters")
        private String code;
    }

    @Data
    public static class ReviewResponse {
        private Long id;
        private String language;
        private String originalCode;
        private String refactoredCode;
        private java.util.List<String> issues;
        private java.util.List<String> suggestions;
        private java.util.List<String> securityWarnings;
        private java.util.List<String> performanceNotes;
        private Integer qualityScore;
        private Integer readabilityScore;
        private Integer securityScore;
        private Integer performanceScore;
        private String summary;
        private String createdAt;
        private String status;
    }

    @Data
    public static class ReviewSummary {
        private Long id;
        private String language;
        private Integer qualityScore;
        private String createdAt;
        private String status;
        // first 100 chars of code
        private String codePreview;
    }
}
