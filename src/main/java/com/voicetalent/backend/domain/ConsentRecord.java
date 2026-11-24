package com.voicetalent.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consent_records")
public class ConsentRecord {

  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID applicationId;

  @Column(nullable = false, length = 64)
  private String consentVersion;

  @Column(nullable = false)
  private Instant acceptedAt;

  protected ConsentRecord() {}

  public ConsentRecord(UUID id, UUID applicationId, String consentVersion, Instant acceptedAt) {
    this.id = id;
    this.applicationId = applicationId;
    this.consentVersion = consentVersion;
    this.acceptedAt = acceptedAt;
  }

  public UUID getId() { return id; }
  public UUID getApplicationId() { return applicationId; }
  public String getConsentVersion() { return consentVersion; }
  public Instant getAcceptedAt() { return acceptedAt; }
}
