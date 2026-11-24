package com.voicetalent.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_reviews")
public class ApplicationReview {

  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID applicationId;

  @Column(nullable = false, length = 120)
  private String reviewerName;

  @Column(nullable = false)
  private int score;

  @Column(length = 2000)
  private String evaluationNotes;

  @Column(nullable = false)
  private Instant createdAt;

  protected ApplicationReview() {}

  public ApplicationReview(
      UUID id,
      UUID applicationId,
      String reviewerName,
      int score,
      String evaluationNotes,
      Instant createdAt
  ) {
    this.id = id;
    this.applicationId = applicationId;
    this.reviewerName = reviewerName;
    this.score = score;
    this.evaluationNotes = evaluationNotes;
    this.createdAt = createdAt;
  }

  public UUID getId() { return id; }
  public UUID getApplicationId() { return applicationId; }
  public String getReviewerName() { return reviewerName; }
  public int getScore() { return score; }
  public String getEvaluationNotes() { return evaluationNotes; }
  public Instant getCreatedAt() { return createdAt; }
}
