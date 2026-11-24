package com.voicetalent.backend.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AddReviewRequest(
    @Min(value = 0, message = "Score minimum 0")
    @Max(value = 100, message = "Score maximum 100")
    int score,

    @Size(max = 2000, message = "Notes trop longues")
    String evaluationNotes
) {}
