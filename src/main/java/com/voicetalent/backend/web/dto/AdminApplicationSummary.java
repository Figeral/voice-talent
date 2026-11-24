package com.voicetalent.backend.web.dto;

import com.voicetalent.backend.domain.Gender;
import com.voicetalent.backend.domain.RegistrationStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AdminApplicationSummary(
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
    String musicGenre,
    String songTitle,
    RegistrationStatus status,
    Instant submittedAt
) {}
