package com.voicetalent.backend.service;

import com.voicetalent.backend.config.AppProperties;
import com.voicetalent.backend.exception.ApiException;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnProperty(name = "app.storage.mode", havingValue = "supabase")
public class SupabaseStorageService implements StorageService {

  private final AppProperties props;
  private final RestClient restClient;

  public SupabaseStorageService(AppProperties props) {
    this.props = props;
    this.restClient = RestClient.builder()
        .baseUrl(props.storage().supabaseUrl())
        .defaultHeader("apikey", props.storage().supabaseServiceKey())
        .defaultHeader("Authorization", "Bearer " + props.storage().supabaseServiceKey())
        .build();
  }

  @Override
  public String createSignedUploadUrl(UUID uploadId, String storagePath, String mimeType) {
    String uri = "/storage/v1/object/upload/sign/" + props.storage().bucket() + "/" + storagePath;
    Map<String, Object> response = restClient.post()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of())
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});

    if (response == null || !response.containsKey("url")) {
      throw new ApiException(HttpStatus.BAD_GATEWAY, "Impossible de préparer l'envoi du fichier.");
    }
    String relative = String.valueOf(response.get("url"));
    return relative.startsWith("http") ? relative : props.storage().supabaseUrl() + "/storage/v1" + relative;
  }

  @Override
  public String createSignedReadUrl(UUID uploadId, String storagePath) {
    String uri = "/storage/v1/object/sign/" + props.storage().bucket() + "/" + storagePath;
    Map<String, Object> response = restClient.post()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("expiresIn", 3600))
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});

    if (response == null || !response.containsKey("signedURL")) {
      return null;
    }
    String relative = String.valueOf(response.get("signedURL"));
    return relative.startsWith("http") ? relative : props.storage().supabaseUrl() + "/storage/v1" + relative;
  }

  @Override
  public boolean isLocal() {
    return false;
  }
}
