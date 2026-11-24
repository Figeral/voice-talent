package com.voicetalent.backend.web.dto;

import com.voicetalent.backend.domain.Gender;
import com.voicetalent.backend.domain.RegistrationStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AdminApplicationDetail(
    UUID id,
    String dossierNo,
    String firstName,
    String lastName,
    String stageName,
    LocalDate birthDate,
    Gender gender,
    String phone,
    String email,
    String city,
    String district,
    String contactName,
    String contactPhone,
    String musicGenre,
    String songTitle,
    String musicBackground,
    RegistrationRequest.SocialLinks socialLinks,
    String photoUrl,
    String presentationUrl,
    RegistrationStatus status,
    Instant submittedAt,
    List<ReviewItem> reviews,
    List<CommentItem> comments,
    List<StatusHistoryItem> statusHistory
) {
  public record ReviewItem(UUID id, String reviewerName, int score, String evaluationNotes, Instant createdAt) {}
  public record CommentItem(UUID id, String authorName, String commentText, boolean privateToRecruiters, Instant createdAt) {}
  public record StatusHistoryItem(UUID id, RegistrationStatus previousStatus, RegistrationStatus newStatus, String changedBy, String comment, Instant createdAt) {}
}
