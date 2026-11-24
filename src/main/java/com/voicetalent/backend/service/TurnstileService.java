package com.voicetalent.backend.service;

import com.voicetalent.backend.config.AppProperties;
import com.voicetalent.backend.exception.ApiException;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class TurnstileService {

  private final AppProperties props;
  private final RestClient restClient;

  public TurnstileService(AppProperties props) {
    this.props = props;
    this.restClient = RestClient.create("https://challenges.cloudflare.com");
  }

  public void verifyOrThrow(String token) {
    if (!props.turnstile().enabled()) {
      return;
    }
    if (token == null || token.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Vérification anti-robot requise.");
    }
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("secret", props.turnstile().secretKey());
    form.add("response", token);

    Map<String, Object> result = restClient.post()
        .uri("/turnstile/v0/siteverify")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(form)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});

    if (result == null || !Boolean.TRUE.equals(result.get("success"))) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Échec de la vérification anti-robot. Réessayez.");
    }
  }
}
