package com.voicetalent.backend.repository;

import com.voicetalent.backend.domain.ApplicationComment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationCommentRepository extends JpaRepository<ApplicationComment, UUID> {
  List<ApplicationComment> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId);
}
