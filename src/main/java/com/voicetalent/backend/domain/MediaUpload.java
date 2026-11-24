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
@Table(name = "media_uploads")
public class MediaUpload {

  @Id
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private UploadKind kind;

  @Column(nullable = false, length = 100)
  private String mimeType;

  @Column(nullable = false)
  private long sizeBytes;

  private Integer durationSeconds;

  @Column(nullable = false, length = 500)
  private String storagePath;

  @Column(nullable = false)
  private boolean uploaded;

  @Column(nullable = false)
  private boolean claimed;

  @Column(nullable = false)
  private Instant createdAt;

  protected MediaUpload() {}

  public MediaUpload(
      UUID id,
      UploadKind kind,
      String mimeType,
      long sizeBytes,
      Integer durationSeconds,
      String storagePath,
      Instant createdAt
  ) {
    this.id = id;
    this.kind = kind;
    this.mimeType = mimeType;
    this.sizeBytes = sizeBytes;
    this.durationSeconds = durationSeconds;
    this.storagePath = storagePath;
    this.uploaded = false;
    this.claimed = false;
    this.createdAt = createdAt;
  }

  public UUID getId() { return id; }
  public UploadKind getKind() { return kind; }
  public String getMimeType() { return mimeType; }
  public long getSizeBytes() { return sizeBytes; }
  public Integer getDurationSeconds() { return durationSeconds; }
  public String getStoragePath() { return storagePath; }
  public boolean isUploaded() { return uploaded; }
  public void setUploaded(boolean uploaded) { this.uploaded = uploaded; }
  public boolean isClaimed() { return claimed; }
  public void setClaimed(boolean claimed) { this.claimed = claimed; }
  public Instant getCreatedAt() { return createdAt; }
}
