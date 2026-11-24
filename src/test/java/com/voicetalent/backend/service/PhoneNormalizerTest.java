package com.voicetalent.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PhoneNormalizerTest {

  @Test
  void normalizesValidCameroonMobileFormats() {
    assertThat(PhoneNormalizer.normalize("6 71 23 45 67")).contains("+237671234567");
    assertThat(PhoneNormalizer.normalize("671234567")).contains("+237671234567");
    assertThat(PhoneNormalizer.normalize("+237 671-23-45-67")).contains("+237671234567");
    assertThat(PhoneNormalizer.normalize("00237699001122")).contains("+237699001122");
    assertThat(PhoneNormalizer.normalize("237655443322")).contains("+237655443322");
  }

  @Test
  void rejectsInvalidNumbers() {
    assertThat(PhoneNormalizer.normalize(null)).isEmpty();
    assertThat(PhoneNormalizer.normalize("12345")).isEmpty();
    assertThat(PhoneNormalizer.normalize("771234567")).isEmpty();
    assertThat(PhoneNormalizer.normalize("+33612345678")).isEmpty();
  }
}
