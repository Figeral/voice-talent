package com.voicetalent.backend.web.dto;

import com.voicetalent.backend.domain.RegistrationStatus;
import java.time.Instant;

public record StatusLookupResponse(
    String dossierNo,
    RegistrationStatus status,
    Instant submittedAt,
    String firstName
) {}
