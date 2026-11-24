package com.voicetalent.backend.service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PhoneNormalizer {
  private static final Pattern CAMEROON_MOBILE = Pattern.compile("^(?:\\+237|00237|237)?(6\\d{8})$");

  private PhoneNormalizer() {}

  public static Optional<String> normalize(String raw) {
    if (raw == null) {
      return Optional.empty();
    }
    String stripped = raw.replaceAll("[\\s.\\-()]", "");
    Matcher matcher = CAMEROON_MOBILE.matcher(stripped);
    if (!matcher.matches()) {
      return Optional.empty();
    }
    return Optional.of("+237" + matcher.group(1));
  }
}
