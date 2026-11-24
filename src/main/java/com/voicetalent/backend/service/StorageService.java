package com.voicetalent.backend.service;

import java.util.UUID;

public interface StorageService {
  String createSignedUploadUrl(UUID uploadId, String storagePath, String mimeType);
  String createSignedReadUrl(UUID uploadId, String storagePath);
  boolean isLocal();
}
