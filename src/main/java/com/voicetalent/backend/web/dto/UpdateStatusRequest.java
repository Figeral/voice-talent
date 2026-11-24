package com.voicetalent.backend.web.dto;

import com.voicetalent.backend.domain.RegistrationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateStatusRequest(
    @NotNull(message = "Statut requis")
    RegistrationStatus status,

    @Size(max = 1000, message = "Commentaire trop long")
    String comment
) {}
