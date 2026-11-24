package com.voicetalent.backend.web;

import com.voicetalent.backend.service.RegistrationService;
import com.voicetalent.backend.web.dto.RegistrationRequest;
import com.voicetalent.backend.web.dto.RegistrationResponse;
import com.voicetalent.backend.web.dto.StatusLookupRequest;
import com.voicetalent.backend.web.dto.StatusLookupResponse;
import com.voicetalent.backend.web.dto.UploadPrepareRequest;
import com.voicetalent.backend.web.dto.UploadPrepareResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
@Tag(name = "Registration", description = "Public candidate registration, signed upload preparation, and dossier status lookup")
public class RegistrationController {

  private final RegistrationService registrationService;

  public RegistrationController(RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  @PostMapping("/uploads")
  public ResponseEntity<UploadPrepareResponse> prepareUpload(
      @Valid @RequestBody UploadPrepareRequest request
  ) {
    return ResponseEntity.ok(registrationService.prepareUpload(request));
  }

  @PostMapping
  public ResponseEntity<RegistrationResponse> submitRegistration(
      @Valid @RequestBody RegistrationRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(registrationService.submitRegistration(request));
  }

  @PostMapping("/status")
  public ResponseEntity<StatusLookupResponse> lookupStatus(
      @Valid @RequestBody StatusLookupRequest request
  ) {
    return ResponseEntity.ok(registrationService.lookupStatus(request));
  }
}
