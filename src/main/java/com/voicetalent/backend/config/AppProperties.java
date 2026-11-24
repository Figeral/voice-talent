package com.voicetalent.backend.config;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    Registration registration,
    Cors cors,
    Storage storage,
    Turnstile turnstile,
    Admin admin,
    Mail mail
) {
  public record Registration(
      OffsetDateTime opensAt,
      OffsetDateTime closesAt,
      boolean enforceWindow,
      LocalDate birthDateMin,
      LocalDate birthDateMax
  ) {}

  public record Cors(List<String> allowedOrigins) {}

  public record Storage(
      String mode,
      String localDir,
      String publicBaseUrl,
      String signingSecret,
      String supabaseUrl,
      String supabaseServiceKey,
      String bucket
  ) {}

  public record Turnstile(boolean enabled, String secretKey) {}

  public record Admin(String username, String password) {}

  public record Mail(boolean enabled, String from) {}
}
