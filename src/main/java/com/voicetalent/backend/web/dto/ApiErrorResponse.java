package com.voicetalent.backend.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
    String error,
    Map<String, String> fieldErrors
) {
  public ApiErrorResponse(String error) {
    this(error, Map.of());
  }
}
