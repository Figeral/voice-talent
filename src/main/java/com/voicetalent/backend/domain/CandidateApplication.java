package com.voicetalent.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "applications")
public class CandidateApplication {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true, length = 32)
  private String dossierNo;

  @Column(nullable = false, length = 80)
  private String lastName;

  @Column(nullable = false, length = 80)
  private String firstName;

  @Column(length = 80)
  private String stageName;

  @Column(nullable = false)
  private LocalDate birthDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private Gender gender;

  @Column(nullable = false, unique = true, length = 20)
  private String phone;

  @Column(nullable = false, unique = true, length = 160)
  private String email;

  @Column(nullable = false, length = 80)
  private String city;

  @Column(nullable = false, length = 80)
  private String district;

  @Column(nullable = false, length = 80)
  private String contactName;

  @Column(nullable = false, length = 20)
  private String contactPhone;

  @Column(nullable = false)
  private UUID photoUploadId;

  @Column(nullable = false, length = 60)
  private String musicGenre;

  @Column(nullable = false, length = 150)
  private String songTitle;

  @Column(nullable = false, length = 1000)
  private String musicBackground;

  @Column(length = 200)
  private String facebookUrl;

  @Column(length = 200)
  private String instagramUrl;

  @Column(length = 200)
  private String tiktokUrl;

  @Column(length = 200)
  private String youtubeUrl;

  @Column(nullable = false)
  private UUID presentationUploadId;

  @Column(nullable = false)
  private boolean acceptRules;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private RegistrationStatus status;

  @Column(nullable = false)
  private Instant submittedAt;

  protected CandidateApplication() {}

  public CandidateApplication(
      UUID id,
      String dossierNo,
      String lastName,
      String firstName,
      String stageName,
      LocalDate birthDate,
      Gender gender,
      String phone,
      String email,
      String city,
      String district,
      String contactName,
      String contactPhone,
      UUID photoUploadId,
      String musicGenre,
      String songTitle,
      String musicBackground,
      String facebookUrl,
      String instagramUrl,
      String tiktokUrl,
      String youtubeUrl,
      UUID presentationUploadId,
      boolean acceptRules,
      RegistrationStatus status,
      Instant submittedAt
  ) {
    this.id = id;
    this.dossierNo = dossierNo;
    this.lastName = lastName;
    this.firstName = firstName;
    this.stageName = stageName;
    this.birthDate = birthDate;
    this.gender = gender;
    this.phone = phone;
    this.email = email;
    this.city = city;
    this.district = district;
    this.contactName = contactName;
    this.contactPhone = contactPhone;
    this.photoUploadId = photoUploadId;
    this.musicGenre = musicGenre;
    this.songTitle = songTitle;
    this.musicBackground = musicBackground;
    this.facebookUrl = facebookUrl;
    this.instagramUrl = instagramUrl;
    this.tiktokUrl = tiktokUrl;
    this.youtubeUrl = youtubeUrl;
    this.presentationUploadId = presentationUploadId;
    this.acceptRules = acceptRules;
    this.status = status;
    this.submittedAt = submittedAt;
  }

  public UUID getId() { return id; }
  public String getDossierNo() { return dossierNo; }
  public String getLastName() { return lastName; }
  public String getFirstName() { return firstName; }
  public String getStageName() { return stageName; }
  public LocalDate getBirthDate() { return birthDate; }
  public Gender getGender() { return gender; }
  public String getPhone() { return phone; }
  public String getEmail() { return email; }
  public String getCity() { return city; }
  public String getDistrict() { return district; }
  public String getContactName() { return contactName; }
  public String getContactPhone() { return contactPhone; }
  public UUID getPhotoUploadId() { return photoUploadId; }
  public String getMusicGenre() { return musicGenre; }
  public String getSongTitle() { return songTitle; }
  public String getMusicBackground() { return musicBackground; }
  public String getFacebookUrl() { return facebookUrl; }
  public String getInstagramUrl() { return instagramUrl; }
  public String getTiktokUrl() { return tiktokUrl; }
  public String getYoutubeUrl() { return youtubeUrl; }
  public UUID getPresentationUploadId() { return presentationUploadId; }
  public boolean isAcceptRules() { return acceptRules; }
  public RegistrationStatus getStatus() { return status; }
  public void setStatus(RegistrationStatus status) { this.status = status; }
  public Instant getSubmittedAt() { return submittedAt; }
}
