package com.voicetalent.backend.service;

import com.voicetalent.backend.config.AppProperties;
import com.voicetalent.backend.exception.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.storage.mode", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

  private final AppProperties props;

  public LocalStorageService(AppProperties props) {
    this.props = props;
  }

  @Override
  public String createSignedUploadUrl(UUID uploadId, String storagePath, String mimeType) {
    String token = sign("upload:" + uploadId);
    return props.storage().publicBaseUrl() + "/api/storage/upload/" + uploadId + "?token=" + token;
  }

  @Override
  public String createSignedReadUrl(UUID uploadId, String storagePath) {
    String token = sign("read:" + uploadId);
    return props.storage().publicBaseUrl() + "/api/storage/files/" + uploadId + "?token=" + token;
  }

  @Override
  public boolean isLocal() {
    return true;
  }

  public void verifyUploadToken(UUID uploadId, String token) {
    if (token == null || !constantTimeEquals(sign("upload:" + uploadId), token)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Jeton de téléversement invalide.");
    }
  }

  public void verifyReadToken(UUID uploadId, String token) {
    if (token == null || !constantTimeEquals(sign("read:" + uploadId), token)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Jeton d'accès invalide.");
    }
  }

  public void store(String storagePath, InputStream inputStream) {
    try {
      Path root = Path.of(props.storage().localDir()).toAbsolutePath().normalize();
      Path target = root.resolve(storagePath).normalize();
      if (!target.startsWith(root)) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Chemin de stockage invalide.");
      }
      Files.createDirectories(target.getParent());
      Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible d'enregistrer le fichier.");
    }
  }

  public Resource load(String storagePath) {
    Path root = Path.of(props.storage().localDir()).toAbsolutePath().normalize();
    Path target = root.resolve(storagePath).normalize();
    if (!target.startsWith(root) || !Files.exists(target)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Fichier introuvable.");
    }
    return new FileSystemResource(target);
  }

  private String sign(String payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec key = new SecretKeySpec(
          props.storage().signingSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      mac.init(key);
      return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("HMAC signing failure", e);
    }
  }

  private boolean constantTimeEquals(String a, String b) {
    return MessageDigest.isEqual(
        a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
  }
}
