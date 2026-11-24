package com.voicetalent.backend.exception;

import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
  private final HttpStatus status;
  private final Map<String, String> fieldErrors;

  public ApiException(HttpStatus status, String message) {
    this(status, message, Collections.emptyMap());
  }

  public ApiException(HttpStatus status, String message, Map<String, String> fieldErrors) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors == null ? Collections.emptyMap() : Map.copyOf(fieldErrors);
  }

  public HttpStatus getStatus() {
    return status;
  }

  public Map<String, String> getFieldErrors() {
    return fieldErrors;
  }
}
