package com.codereview.repository;

import com.codereview.entity.CodeReview;
import com.codereview.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {

    Page<CodeReview> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<CodeReview> findTop10ByUserOrderByCreatedAtDesc(User user);

    Optional<CodeReview> findByIdAndUser(Long id, User user);

    long countByUser(User user);

    @Query("SELECT AVG(c.qualityScore) FROM CodeReview c WHERE c.user = :user AND c.qualityScore IS NOT NULL")
    Double avgQualityScoreByUser(User user);

    @Query("SELECT c.language, COUNT(c) FROM CodeReview c WHERE c.user = :user GROUP BY c.language")
    List<Object[]> countByLanguageForUser(User user);
}
