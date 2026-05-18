package com.codereview.controller;

import com.codereview.dto.ReviewDto;
import com.codereview.service.CodeReviewService;
import com.codereview.service.PdfReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final CodeReviewService reviewService;
    private final PdfReportService pdfReportService;

    /**
     * POST /api/reviews
     * Submit code for AI review
     */
    @PostMapping
    public ResponseEntity<ReviewDto.ReviewResponse> review(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewDto.ReviewRequest request) {

        ReviewDto.ReviewResponse response =
                reviewService.reviewCode(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/reviews/{id}/refactor
     * Get AI-refactored version of the code
     */
    @PostMapping("/{id}/refactor")
    public ResponseEntity<Map<String, String>> refactor(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        String refactored = reviewService.refactorCode(userDetails.getUsername(), id);
        return ResponseEntity.ok(Map.of("refactoredCode", refactored));
    }

    /**
     * GET /api/reviews/history?page=0&size=10
     * Get review history for current user
     */
    @GetMapping("/history")
    public ResponseEntity<List<ReviewDto.ReviewSummary>> history(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(reviewService.getHistory(userDetails.getUsername(), page, size));
    }

    /**
     * GET /api/reviews/{id}
     * Get a specific review
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto.ReviewResponse> getReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(reviewService.getReview(userDetails.getUsername(), id));
    }

    /**
     * GET /api/reviews/dashboard
     * Get dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(reviewService.getDashboardStats(userDetails.getUsername()));
    }

    /**
     * GET /api/reviews/{id}/pdf
     * Download PDF report
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        ReviewDto.ReviewResponse review = reviewService.getReview(userDetails.getUsername(), id);
        byte[] pdf = pdfReportService.generateReport(review);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=code-review-" + id + ".pdf")
                .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_PDF))
                .body(pdf);
    }

    /**
     * DELETE /api/reviews/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        reviewService.deleteReview(userDetails.getUsername(), id);
        return ResponseEntity.ok(Map.of("message", "Review deleted"));
    }
}
