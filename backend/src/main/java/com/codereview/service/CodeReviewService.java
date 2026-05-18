package com.codereview.service;

import com.codereview.ai.AiResponseParser;
import com.codereview.ai.GroqAiService;
import com.codereview.dto.ReviewDto;
import com.codereview.entity.CodeReview;
import com.codereview.entity.User;
import com.codereview.repository.CodeReviewRepository;
import com.codereview.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CodeReviewService {

    private final CodeReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final GroqAiService aiService;
    private final AiResponseParser parser;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public ReviewDto.ReviewResponse reviewCode(String username, ReviewDto.ReviewRequest request) {
        User user = getUser(username);

        // Save initial record
        CodeReview review = CodeReview.builder()
                .user(user)
                .language(request.getLanguage())
                .originalCode(request.getCode())
                .status(CodeReview.ReviewStatus.PENDING)
                .build();
        review = reviewRepository.save(java.util.Objects.requireNonNull(review));

        try {
            log.info("Sending code to Groq AI for user: {}", username);
            String aiResponse = aiService.analyzeCode(request.getLanguage(), request.getCode());
            AiResponseParser.ParsedReview parsed = parser.parse(aiResponse);

            // Update with AI results
            review.setQualityScore(parsed.getQualityScore());
            review.setReadabilityScore(parsed.getReadabilityScore());
            review.setSecurityScore(parsed.getSecurityScore());
            review.setPerformanceScore(parsed.getPerformanceScore());
            review.setSummary(parsed.getSummary());
            review.setIssues(toJson(parsed.getIssues()));
            review.setSuggestions(toJson(parsed.getSuggestions()));
            review.setSecurityWarnings(toJson(parsed.getSecurityWarnings()));
            review.setPerformanceNotes(toJson(parsed.getPerformanceNotes()));
            review.setStatus(CodeReview.ReviewStatus.COMPLETED);

            review = reviewRepository.save(review);
            return toResponse(review, parsed);

        } catch (Exception e) {
            log.error("Review failed: {}", e.getMessage());
            review.setStatus(CodeReview.ReviewStatus.FAILED);
            reviewRepository.save(review);
            throw new RuntimeException("Code review failed: " + e.getMessage());
        }
    }

    @Transactional
    public String refactorCode(String username, Long reviewId) {
        User user = getUser(username);
        CodeReview review = reviewRepository.findByIdAndUser(reviewId, user)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        String refactored = aiService.refactorCode(review.getLanguage(), review.getOriginalCode());
        review.setRefactoredCode(refactored);
        reviewRepository.save(review);
        return refactored;
    }

    public List<ReviewDto.ReviewSummary> getHistory(String username, int page, int size) {
        User user = getUser(username);
        Page<CodeReview> reviews = reviewRepository.findByUserOrderByCreatedAtDesc(
                user, PageRequest.of(page, size));

        return reviews.stream().map(this::toSummary).collect(Collectors.toList());
    }

    public ReviewDto.ReviewResponse getReview(String username, Long id) {
        User user = getUser(username);
        CodeReview review = reviewRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return toResponse(review, null);
    }

    public Map<String, Object> getDashboardStats(String username) {
        User user = getUser(username);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReviews", reviewRepository.countByUser(user));
        stats.put("avgQualityScore", reviewRepository.avgQualityScoreByUser(user));

        List<Object[]> langCounts = reviewRepository.countByLanguageForUser(user);
        Map<String, Long> langMap = new HashMap<>();
        langCounts.forEach(row -> langMap.put((String) row[0], (Long) row[1]));
        stats.put("byLanguage", langMap);

        List<ReviewDto.ReviewSummary> recent = getHistory(username, 0, 5);
        stats.put("recentReviews", recent);

        return stats;
    }

    public void deleteReview(String username, Long id) {
        User user = getUser(username);
        CodeReview review = reviewRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        reviewRepository.delete(java.util.Objects.requireNonNull(review));
    }

    // ---- Helpers ----

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> fromJson(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    private ReviewDto.ReviewResponse toResponse(CodeReview review, AiResponseParser.ParsedReview parsed) {
        ReviewDto.ReviewResponse response = new ReviewDto.ReviewResponse();
        response.setId(review.getId());
        response.setLanguage(review.getLanguage());
        response.setOriginalCode(review.getOriginalCode());
        response.setRefactoredCode(review.getRefactoredCode());
        response.setQualityScore(review.getQualityScore());
        response.setReadabilityScore(review.getReadabilityScore());
        response.setSecurityScore(review.getSecurityScore());
        response.setPerformanceScore(review.getPerformanceScore());
        response.setSummary(review.getSummary());
        response.setIssues(fromJson(review.getIssues()));
        response.setSuggestions(fromJson(review.getSuggestions()));
        response.setSecurityWarnings(fromJson(review.getSecurityWarnings()));
        response.setPerformanceNotes(fromJson(review.getPerformanceNotes()));
        response.setStatus(review.getStatus().name());
        if (review.getCreatedAt() != null) {
            response.setCreatedAt(review.getCreatedAt().format(FORMATTER));
        }
        return response;
    }

    private ReviewDto.ReviewSummary toSummary(CodeReview review) {
        ReviewDto.ReviewSummary summary = new ReviewDto.ReviewSummary();
        summary.setId(review.getId());
        summary.setLanguage(review.getLanguage());
        summary.setQualityScore(review.getQualityScore());
        summary.setStatus(review.getStatus().name());
        if (review.getCreatedAt() != null) {
            summary.setCreatedAt(review.getCreatedAt().format(FORMATTER));
        }
        String code = review.getOriginalCode();
        summary.setCodePreview(code != null && code.length() > 100 ? code.substring(0, 100) + "..." : code);
        return summary;
    }
}
