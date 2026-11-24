package com.voicetalent.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_comments")
public class ApplicationComment {

  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID applicationId;

  @Column(nullable = false, length = 120)
  private String authorName;

  @Column(nullable = false, length = 2000)
  private String commentText;

  @Column(nullable = false)
  private boolean privateToRecruiters;

  @Column(nullable = false)
  private Instant createdAt;

  protected ApplicationComment() {}

  public ApplicationComment(
      UUID id,
      UUID applicationId,
      String authorName,
      String commentText,
      boolean privateToRecruiters,
      Instant createdAt
  ) {
    this.id = id;
    this.applicationId = applicationId;
    this.authorName = authorName;
    this.commentText = commentText;
    this.privateToRecruiters = privateToRecruiters;
    this.createdAt = createdAt;
  }

  public UUID getId() { return id; }
  public UUID getApplicationId() { return applicationId; }
  public String getAuthorName() { return authorName; }
  public String getCommentText() { return commentText; }
  public boolean isPrivateToRecruiters() { return privateToRecruiters; }
  public Instant getCreatedAt() { return createdAt; }
}
