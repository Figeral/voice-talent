package com.voicetalent.backend.web;

import com.voicetalent.backend.domain.MediaUpload;
import com.voicetalent.backend.exception.ApiException;
import com.voicetalent.backend.repository.MediaUploadRepository;
import com.voicetalent.backend.service.LocalStorageService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequestMapping("/api/storage")
@ConditionalOnBean(LocalStorageService.class)
public class LocalStorageController {

  private final LocalStorageService storageService;
  private final MediaUploadRepository uploadRepo;

  public LocalStorageController(LocalStorageService storageService, MediaUploadRepository uploadRepo) {
    this.storageService = storageService;
    this.uploadRepo = uploadRepo;
  }

  @PutMapping("/upload/{uploadId}")
  public ResponseEntity<Void> uploadFile(
      @PathVariable UUID uploadId,
      @RequestParam String token,
      HttpServletRequest request
  ) throws IOException {
    storageService.verifyUploadToken(uploadId, token);

    MediaUpload upload = uploadRepo.findById(uploadId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Téléversement introuvable."));

    if (upload.isUploaded()) {
      throw new ApiException(HttpStatus.CONFLICT, "Fichier déjà envoyé.");
    }

    if (request instanceof MultipartHttpServletRequest multipartRequest) {
      MultipartFile file = multipartRequest.getFileMap().values().stream()
          .findFirst()
          .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Aucun fichier fourni."));
      try (InputStream in = file.getInputStream()) {
        storageService.store(upload.getStoragePath(), in);
      }
    } else {
      try (InputStream in = request.getInputStream()) {
        storageService.store(upload.getStoragePath(), in);
      }
    }

    upload.setUploaded(true);
    uploadRepo.save(upload);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/files/{uploadId}")
  public ResponseEntity<Resource> downloadFile(
      @PathVariable UUID uploadId,
      @RequestParam String token
  ) {
    storageService.verifyReadToken(uploadId, token);

    MediaUpload upload = uploadRepo.findById(uploadId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Fichier introuvable."));

    Resource resource = storageService.load(upload.getStoragePath());
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(upload.getMimeType()))
        .body(resource);
  }
}
