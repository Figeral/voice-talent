package com.voicetalent.backend.repository;

import com.voicetalent.backend.domain.ApplicationReview;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationReviewRepository extends JpaRepository<ApplicationReview, UUID> {
  List<ApplicationReview> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId);
}
