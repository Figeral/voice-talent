package com.voicetalent.backend.web.dto;

import com.voicetalent.backend.domain.UploadKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UploadPrepareRequest(
    @NotNull(message = "Type de fichier requis")
    UploadKind kind,

    @NotBlank(message = "Type MIME requis")
    String mime,

    @Positive(message = "Fichier vide.")
    long size,

    Integer durationSeconds
) {}
