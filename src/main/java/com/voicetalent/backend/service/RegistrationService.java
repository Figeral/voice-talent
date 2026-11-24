package com.voicetalent.backend.service;

import com.voicetalent.backend.config.AppProperties;
import com.voicetalent.backend.domain.ApplicationStatusHistory;
import com.voicetalent.backend.domain.CandidateApplication;
import com.voicetalent.backend.domain.ConsentRecord;
import com.voicetalent.backend.domain.MediaUpload;
import com.voicetalent.backend.domain.RegistrationStatus;
import com.voicetalent.backend.domain.UploadKind;
import com.voicetalent.backend.exception.ApiException;
import com.voicetalent.backend.repository.ApplicationStatusHistoryRepository;
import com.voicetalent.backend.repository.CandidateApplicationRepository;
import com.voicetalent.backend.repository.ConsentRecordRepository;
import com.voicetalent.backend.repository.MediaUploadRepository;
import com.voicetalent.backend.web.dto.RegistrationRequest;
import com.voicetalent.backend.web.dto.RegistrationResponse;
import com.voicetalent.backend.web.dto.StatusLookupRequest;
import com.voicetalent.backend.web.dto.StatusLookupResponse;
import com.voicetalent.backend.web.dto.UploadPrepareRequest;
import com.voicetalent.backend.web.dto.UploadPrepareResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

  private static final long PHOTO_MAX_BYTES = 5L * 1024 * 1024;
  private static final long VIDEO_MAX_BYTES = 40L * 1024 * 1024;
  private static final int VIDEO_MIN_SECONDS = 13;
  private static final int VIDEO_MAX_SECONDS = 32;
  private static final Pattern HTTPS_URL = Pattern.compile("^https://\\S+$");

  private static final Map<String, String> PHOTO_MIMES = Map.of(
      "image/jpeg", "jpg",
      "image/png", "png",
      "image/webp", "webp"
  );

  private static final Map<String, String> VIDEO_MIMES = Map.of(
      "video/mp4", "mp4",
      "video/quicktime", "mov",
      "video/webm", "webm",
      "video/3gpp", "3gp"
  );

  private final AppProperties props;
  private final Clock clock;
  private final MediaUploadRepository uploadRepo;
  private final CandidateApplicationRepository appRepo;
  private final ConsentRecordRepository consentRepo;
  private final ApplicationStatusHistoryRepository historyRepo;
  private final StorageService storageService;
  private final TurnstileService turnstileService;
  private final EmailService emailService;

  public RegistrationService(
      AppProperties props,
      Clock clock,
      MediaUploadRepository uploadRepo,
      CandidateApplicationRepository appRepo,
      ConsentRecordRepository consentRepo,
      ApplicationStatusHistoryRepository historyRepo,
      StorageService storageService,
      TurnstileService turnstileService,
      EmailService emailService
  ) {
    this.props = props;
    this.clock = clock;
    this.uploadRepo = uploadRepo;
    this.appRepo = appRepo;
    this.consentRepo = consentRepo;
    this.historyRepo = historyRepo;
    this.storageService = storageService;
    this.turnstileService = turnstileService;
    this.emailService = emailService;
  }

  @Transactional
  public UploadPrepareResponse prepareUpload(UploadPrepareRequest req) {
    Map<String, String> allowed = req.kind() == UploadKind.photo ? PHOTO_MIMES : VIDEO_MIMES;
    long maxBytes = req.kind() == UploadKind.photo ? PHOTO_MAX_BYTES : VIDEO_MAX_BYTES;

    String ext = allowed.get(req.mime());
    if (ext == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Format de fichier non accepté.");
    }
    if (req.size() > maxBytes) {
      long maxMb = Math.round((double) maxBytes / (1024 * 1024));
      throw new ApiException(HttpStatus.BAD_REQUEST, "Fichier trop volumineux (maximum " + maxMb + " Mo).");
    }
    if (req.kind() == UploadKind.presentation && req.durationSeconds() != null) {
      int d = req.durationSeconds();
      if (d < VIDEO_MIN_SECONDS) {
        throw new ApiException(HttpStatus.BAD_REQUEST,
            "Vidéo trop courte (" + d + " s) : elle doit durer au moins 15 secondes.");
      }
      if (d > VIDEO_MAX_SECONDS) {
        throw new ApiException(HttpStatus.BAD_REQUEST,
            "Vidéo trop longue (" + d + " s) : elle doit durer 30 secondes maximum.");
      }
    }

    UUID uploadId = UUID.randomUUID();
    String storagePath = "douala-2026/" + req.kind().name() + "/" + uploadId + "." + ext;
    MediaUpload upload = new MediaUpload(
        uploadId,
        req.kind(),
        req.mime(),
        req.size(),
        req.durationSeconds(),
        storagePath,
        clock.instant()
    );
    uploadRepo.save(upload);

    String signedUrl = storageService.createSignedUploadUrl(uploadId, storagePath, req.mime());
    return new UploadPrepareResponse(uploadId, signedUrl);
  }

  @Transactional
  public RegistrationResponse submitRegistration(RegistrationRequest req) {
    Instant now = clock.instant();
    if (props.registration().enforceWindow()) {
      Instant opens = props.registration().opensAt().toInstant();
      Instant closes = props.registration().closesAt().toInstant();
      if (now.isBefore(opens)) {
        throw new ApiException(HttpStatus.FORBIDDEN, "Les inscriptions ne sont pas encore ouvertes.");
      }
      if (now.isAfter(closes)) {
        throw new ApiException(HttpStatus.FORBIDDEN, "Les inscriptions sont closes.");
      }
    }

    if (req.website() != null && !req.website().isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Requête invalide.");
    }

    turnstileService.verifyOrThrow(req.turnstileToken());

    Map<String, String> fieldErrors = new LinkedHashMap<>();

    LocalDate minDate = props.registration().birthDateMin();
    LocalDate maxDate = props.registration().birthDateMax();
    if (req.birthDate().isBefore(minDate) || req.birthDate().isAfter(maxDate)) {
      fieldErrors.put("birthDate", "Le concours est ouvert aux personnes âgées de 18 à 35 ans.");
    }

    String normalizedPhone = PhoneNormalizer.normalize(req.phone()).orElse(null);
    if (normalizedPhone == null) {
      fieldErrors.put("phone", "Numéro mobile camerounais attendu (ex. 6 71 23 45 67)");
    }

    String normalizedContactPhone = PhoneNormalizer.normalize(req.contactPhone()).orElse(null);
    if (normalizedContactPhone == null) {
      fieldErrors.put("contactPhone", "Numéro mobile camerounais attendu (ex. 6 71 23 45 67)");
    }

    String normalizedEmail = req.email().trim().toLowerCase();

    String fb = validateSocialUrl(req.socialLinks() != null ? req.socialLinks().facebook() : null, "socialLinks.facebook", fieldErrors);
    String ig = validateSocialUrl(req.socialLinks() != null ? req.socialLinks().instagram() : null, "socialLinks.instagram", fieldErrors);
    String tt = validateSocialUrl(req.socialLinks() != null ? req.socialLinks().tiktok() : null, "socialLinks.tiktok", fieldErrors);
    String yt = validateSocialUrl(req.socialLinks() != null ? req.socialLinks().youtube() : null, "socialLinks.youtube", fieldErrors);

    if (!fieldErrors.isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Veuillez corriger les champs signalés.", fieldErrors);
    }

    if (appRepo.existsByPhone(normalizedPhone)) {
      fieldErrors.put("phone", "Un dossier existe déjà avec ce numéro de téléphone.");
    }
    if (appRepo.existsByEmail(normalizedEmail)) {
      fieldErrors.put("email", "Un dossier existe déjà avec cette adresse e-mail.");
    }
    if (!fieldErrors.isEmpty()) {
      throw new ApiException(HttpStatus.CONFLICT, "Un seul dossier est admis par personne.", fieldErrors);
    }

    MediaUpload photo = validateClaimableUpload(req.photoUploadId(), UploadKind.photo, "photoUploadId");
    MediaUpload presentation = validateClaimableUpload(req.presentationUploadId(), UploadKind.presentation, "presentationUploadId");

    String dossierNo = generateDossierNo();
    UUID appId = UUID.randomUUID();
    String stageName = req.stageName() != null && !req.stageName().trim().isEmpty()
        ? req.stageName().trim()
        : null;

    CandidateApplication app = new CandidateApplication(
        appId,
        dossierNo,
        req.lastName().trim(),
        req.firstName().trim(),
        stageName,
        req.birthDate(),
        req.gender(),
        normalizedPhone,
        normalizedEmail,
        req.city().trim(),
        req.district().trim(),
        req.contactName().trim(),
        normalizedContactPhone,
        photo.getId(),
        req.musicGenre().trim(),
        req.songTitle().trim(),
        req.musicBackground().trim(),
        fb,
        ig,
        tt,
        yt,
        presentation.getId(),
        req.acceptRules(),
        RegistrationStatus.received,
        now
    );

    appRepo.save(app);

    photo.setClaimed(true);
    presentation.setClaimed(true);
    uploadRepo.save(photo);
    uploadRepo.save(presentation);

    consentRepo.save(new ConsentRecord(UUID.randomUUID(), appId, "v2.0-Douala-2026", now));
    historyRepo.save(new ApplicationStatusHistory(
        UUID.randomUUID(), appId, null, RegistrationStatus.received, "system", "Dossier enregistré", now));

    emailService.sendRegistrationConfirmation(normalizedEmail, app.getFirstName(), dossierNo);

    return new RegistrationResponse(dossierNo);
  }

  @Transactional(readOnly = true)
  public StatusLookupResponse lookupStatus(StatusLookupRequest req) {
    String dossierNo = req.dossierNo().trim().toUpperCase();
    String phone = PhoneNormalizer.normalize(req.phone())
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
            "Numéro mobile camerounais attendu (ex. 6 71 23 45 67)"));

    CandidateApplication app = appRepo.findByDossierNoAndPhone(dossierNo, phone)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
            "Aucun dossier trouvé avec ces informations."));

    return new StatusLookupResponse(
        app.getDossierNo(),
        app.getStatus(),
        app.getSubmittedAt(),
        app.getFirstName()
    );
  }

  private MediaUpload validateClaimableUpload(UUID uploadId, UploadKind expectedKind, String fieldName) {
    MediaUpload upload = uploadRepo.findById(uploadId)
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
            "Fichier introuvable. Veuillez le renvoyer.",
            Map.of(fieldName, "Fichier introuvable. Veuillez le renvoyer.")));

    if (upload.getKind() != expectedKind) {
      throw new ApiException(HttpStatus.BAD_REQUEST,
          "Type de fichier invalide.",
          Map.of(fieldName, "Type de fichier invalide."));
    }
    if (upload.isClaimed()) {
      throw new ApiException(HttpStatus.CONFLICT,
          "Ce fichier est déjà associé à une candidature.",
          Map.of(fieldName, "Ce fichier est déjà utilisé."));
    }
    if (storageService.isLocal() && !upload.isUploaded()) {
      throw new ApiException(HttpStatus.BAD_REQUEST,
          "L'envoi du fichier n'est pas terminé.",
          Map.of(fieldName, "L'envoi du fichier n'est pas terminé."));
    }
    return upload;
  }

  private String validateSocialUrl(String raw, String fieldName, Map<String, String> fieldErrors) {
    if (raw == null || raw.trim().isEmpty()) {
      return null;
    }
    String trimmed = raw.trim();
    if (!HTTPS_URL.matcher(trimmed).matches()) {
      fieldErrors.put(fieldName, "Lien invalide (doit commencer par https://)");
      return null;
    }
    return trimmed;
  }

  private String generateDossierNo() {
    long next = appRepo.count() + 1;
    return String.format("VT-2026-%04d", next);
  }
}
