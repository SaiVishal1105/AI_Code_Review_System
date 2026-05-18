package com.codereview.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String language;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String originalCode;

    @Column(columnDefinition = "TEXT")
    private String refactoredCode;

    // JSON stored as text for simplicity
    @Column(columnDefinition = "TEXT")
    private String issues;

    @Column(columnDefinition = "TEXT")
    private String suggestions;

    @Column(columnDefinition = "TEXT")
    private String securityWarnings;

    @Column(columnDefinition = "TEXT")
    private String performanceNotes;

    private Integer qualityScore;

    private Integer readabilityScore;

    private Integer securityScore;

    private Integer performanceScore;

    @Column(name = "review_summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    public enum ReviewStatus {
        PENDING, COMPLETED, FAILED
    }
}
