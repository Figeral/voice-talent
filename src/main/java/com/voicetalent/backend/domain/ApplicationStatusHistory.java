package com.voicetalent.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_status_history")
public class ApplicationStatusHistory {

  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID applicationId;

  @Enumerated(EnumType.STRING)
  @Column(length = 32)
  private RegistrationStatus previousStatus;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private RegistrationStatus newStatus;

  @Column(nullable = false, length = 120)
  private String changedBy;

  @Column(length = 1000)
  private String comment;

  @Column(nullable = false)
  private Instant createdAt;

  protected ApplicationStatusHistory() {}

  public ApplicationStatusHistory(
      UUID id,
      UUID applicationId,
      RegistrationStatus previousStatus,
      RegistrationStatus newStatus,
      String changedBy,
      String comment,
      Instant createdAt
  ) {
    this.id = id;
    this.applicationId = applicationId;
    this.previousStatus = previousStatus;
    this.newStatus = newStatus;
    this.changedBy = changedBy;
    this.comment = comment;
    this.createdAt = createdAt;
  }

  public UUID getId() { return id; }
  public UUID getApplicationId() { return applicationId; }
  public RegistrationStatus getPreviousStatus() { return previousStatus; }
  public RegistrationStatus getNewStatus() { return newStatus; }
  public String getChangedBy() { return changedBy; }
  public String getComment() { return comment; }
  public Instant getCreatedAt() { return createdAt; }
}
