package com.voicetalent.backend.web.dto;

import java.util.UUID;

public record UploadPrepareResponse(UUID uploadId, String signedUrl) {}
