package com.voicetalent.backend.service;

import com.voicetalent.backend.domain.ApplicationComment;
import com.voicetalent.backend.domain.ApplicationReview;
import com.voicetalent.backend.domain.ApplicationStatusHistory;
import com.voicetalent.backend.domain.CandidateApplication;
import com.voicetalent.backend.domain.Gender;
import com.voicetalent.backend.domain.MediaUpload;
import com.voicetalent.backend.domain.RegistrationStatus;
import com.voicetalent.backend.exception.ApiException;
import com.voicetalent.backend.repository.ApplicationCommentRepository;
import com.voicetalent.backend.repository.ApplicationReviewRepository;
import com.voicetalent.backend.repository.ApplicationStatusHistoryRepository;
import com.voicetalent.backend.repository.CandidateApplicationRepository;
import com.voicetalent.backend.repository.MediaUploadRepository;
import com.voicetalent.backend.web.dto.AddReviewRequest;
import com.voicetalent.backend.web.dto.AdminApplicationDetail;
import com.voicetalent.backend.web.dto.AdminApplicationSummary;
import com.voicetalent.backend.web.dto.RegistrationRequest;
import com.voicetalent.backend.web.dto.UpdateStatusRequest;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

  private final Clock clock;
  private final CandidateApplicationRepository appRepo;
  private final MediaUploadRepository uploadRepo;
  private final ApplicationReviewRepository reviewRepo;
  private final ApplicationCommentRepository commentRepo;
  private final ApplicationStatusHistoryRepository historyRepo;
  private final StorageService storageService;

  public AdminService(
      Clock clock,
      CandidateApplicationRepository appRepo,
      MediaUploadRepository uploadRepo,
      ApplicationReviewRepository reviewRepo,
      ApplicationCommentRepository commentRepo,
      ApplicationStatusHistoryRepository historyRepo,
      StorageService storageService
  ) {
    this.clock = clock;
    this.appRepo = appRepo;
    this.uploadRepo = uploadRepo;
    this.reviewRepo = reviewRepo;
    this.commentRepo = commentRepo;
    this.historyRepo = historyRepo;
    this.storageService = storageService;
  }

  @Transactional(readOnly = true)
  public List<AdminApplicationSummary> listApplications(RegistrationStatus status, Gender gender, String query) {
    String q = query == null ? "" : query.trim().toLowerCase();
    return appRepo.findAllByOrderBySubmittedAtDesc().stream()
        .filter(a -> status == null || a.getStatus() == status)
        .filter(a -> gender == null || a.getGender() == gender)
        .filter(a -> q.isEmpty()
            || a.getDossierNo().toLowerCase().contains(q)
            || a.getFirstName().toLowerCase().contains(q)
            || a.getLastName().toLowerCase().contains(q)
            || (a.getStageName() != null && a.getStageName().toLowerCase().contains(q))
            || a.getPhone().contains(q)
            || a.getEmail().toLowerCase().contains(q))
        .map(this::toSummary)
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminApplicationDetail getApplicationDetail(UUID id) {
    CandidateApplication app = appRepo.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Candidature introuvable."));

    String photoUrl = resolveSignedReadUrl(app.getPhotoUploadId());
    String presentationUrl = resolveSignedReadUrl(app.getPresentationUploadId());

    var reviews = reviewRepo.findByApplicationIdOrderByCreatedAtDesc(id).stream()
        .map(r -> new AdminApplicationDetail.ReviewItem(
            r.getId(), r.getReviewerName(), r.getScore(), r.getEvaluationNotes(), r.getCreatedAt()))
        .toList();

    var comments = commentRepo.findByApplicationIdOrderByCreatedAtDesc(id).stream()
        .map(c -> new AdminApplicationDetail.CommentItem(
            c.getId(), c.getAuthorName(), c.getCommentText(), c.isPrivateToRecruiters(), c.getCreatedAt()))
        .toList();

    var history = historyRepo.findByApplicationIdOrderByCreatedAtAsc(id).stream()
        .map(h -> new AdminApplicationDetail.StatusHistoryItem(
            h.getId(), h.getPreviousStatus(), h.getNewStatus(), h.getChangedBy(), h.getComment(), h.getCreatedAt()))
        .toList();

    return new AdminApplicationDetail(
        app.getId(),
        app.getDossierNo(),
        app.getFirstName(),
        app.getLastName(),
        app.getStageName(),
        app.getBirthDate(),
        app.getGender(),
        app.getPhone(),
        app.getEmail(),
        app.getCity(),
        app.getDistrict(),
        app.getContactName(),
        app.getContactPhone(),
        app.getMusicGenre(),
        app.getSongTitle(),
        app.getMusicBackground(),
        new RegistrationRequest.SocialLinks(
            app.getFacebookUrl(), app.getInstagramUrl(), app.getTiktokUrl(), app.getYoutubeUrl()),
        photoUrl,
        presentationUrl,
        app.getStatus(),
        app.getSubmittedAt(),
        reviews,
        comments,
        history
    );
  }

  @Transactional
  public AdminApplicationSummary updateStatus(UUID id, UpdateStatusRequest req, String actor) {
    CandidateApplication app = appRepo.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Candidature introuvable."));

    RegistrationStatus previous = app.getStatus();
    app.setStatus(req.status());
    appRepo.save(app);

    Instant now = clock.instant();
    historyRepo.save(new ApplicationStatusHistory(
        UUID.randomUUID(), id, previous, req.status(), actor, req.comment(), now));

    if (req.comment() != null && !req.comment().isBlank()) {
      commentRepo.save(new ApplicationComment(
          UUID.randomUUID(), id, actor, req.comment().trim(), true, now));
    }

    return toSummary(app);
  }

  @Transactional
  public AdminApplicationDetail.ReviewItem addReview(UUID id, AddReviewRequest req, String reviewerName) {
    if (!appRepo.existsById(id)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Candidature introuvable.");
    }
    ApplicationReview review = new ApplicationReview(
        UUID.randomUUID(),
        id,
        reviewerName,
        req.score(),
        req.evaluationNotes() != null ? req.evaluationNotes().trim() : null,
        clock.instant()
    );
    reviewRepo.save(review);
    return new AdminApplicationDetail.ReviewItem(
        review.getId(), review.getReviewerName(), review.getScore(), review.getEvaluationNotes(), review.getCreatedAt());
  }

  @Transactional(readOnly = true)
  public String exportCsv() {
    StringBuilder sb = new StringBuilder();
    sb.append("dossierNo,firstName,lastName,stageName,birthDate,gender,phone,email,city,district,musicGenre,songTitle,status,submittedAt\n");
    for (CandidateApplication a : appRepo.findAllByOrderBySubmittedAtDesc()) {
      sb.append(csv(a.getDossierNo())).append(',')
          .append(csv(a.getFirstName())).append(',')
          .append(csv(a.getLastName())).append(',')
          .append(csv(a.getStageName())).append(',')
          .append(csv(String.valueOf(a.getBirthDate()))).append(',')
          .append(csv(a.getGender().name())).append(',')
          .append(csv(a.getPhone())).append(',')
          .append(csv(a.getEmail())).append(',')
          .append(csv(a.getCity())).append(',')
          .append(csv(a.getDistrict())).append(',')
          .append(csv(a.getMusicGenre())).append(',')
          .append(csv(a.getSongTitle())).append(',')
          .append(csv(a.getStatus().name())).append(',')
          .append(csv(String.valueOf(a.getSubmittedAt())))
          .append('\n');
    }
    return sb.toString();
  }

  private String resolveSignedReadUrl(UUID uploadId) {
    return uploadRepo.findById(uploadId)
        .map(MediaUpload::getStoragePath)
        .map(path -> storageService.createSignedReadUrl(uploadId, path))
        .orElse(null);
  }

  private AdminApplicationSummary toSummary(CandidateApplication a) {
    return new AdminApplicationSummary(
        a.getId(),
        a.getDossierNo(),
        a.getFirstName(),
        a.getLastName(),
        a.getStageName(),
        a.getBirthDate(),
        a.getGender(),
        a.getPhone(),
        a.getEmail(),
        a.getCity(),
        a.getDistrict(),
        a.getMusicGenre(),
        a.getSongTitle(),
        a.getStatus(),
        a.getSubmittedAt()
    );
  }

  private static String csv(String val) {
    if (val == null) return "";
    return "\"" + val.replace("\"", "\"\"") + "\"";
  }
}
