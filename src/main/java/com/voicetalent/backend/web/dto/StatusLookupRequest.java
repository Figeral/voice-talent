package com.voicetalent.backend.web.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusLookupRequest(
    @NotBlank(message = "Numéro de dossier requis")
    String dossierNo,

    @NotBlank(message = "Téléphone requis")
    String phone
) {}
