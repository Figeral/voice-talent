package com.voicetalent.backend.web;

import com.voicetalent.backend.domain.Gender;
import com.voicetalent.backend.domain.RegistrationStatus;
import com.voicetalent.backend.service.AdminService;
import com.voicetalent.backend.web.dto.AddReviewRequest;
import com.voicetalent.backend.web.dto.AdminApplicationDetail;
import com.voicetalent.backend.web.dto.AdminApplicationSummary;
import com.voicetalent.backend.web.dto.UpdateStatusRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/applications")
@Tag(name = "Admin & Jury", description = "Protected back-office endpoints for reviewing, scoring, and exporting candidate applications")
@SecurityRequirement(name = "basicAuth")
public class AdminController {

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping
  public ResponseEntity<List<AdminApplicationSummary>> listApplications(
      @RequestParam(required = false) RegistrationStatus status,
      @RequestParam(required = false) Gender gender,
      @RequestParam(required = false) String q
  ) {
    return ResponseEntity.ok(adminService.listApplications(status, gender, q));
  }

  @GetMapping("/{id}")
  public ResponseEntity<AdminApplicationDetail> getApplication(@PathVariable UUID id) {
    return ResponseEntity.ok(adminService.getApplicationDetail(id));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<AdminApplicationSummary> updateStatus(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateStatusRequest request,
      Principal principal
  ) {
    String actor = principal != null ? principal.getName() : "admin";
    return ResponseEntity.ok(adminService.updateStatus(id, request, actor));
  }

  @PostMapping("/{id}/reviews")
  public ResponseEntity<AdminApplicationDetail.ReviewItem> addReview(
      @PathVariable UUID id,
      @Valid @RequestBody AddReviewRequest request,
      Principal principal
  ) {
    String reviewer = principal != null ? principal.getName() : "admin";
    return ResponseEntity.ok(adminService.addReview(id, request, reviewer));
  }

  @GetMapping("/export.csv")
  public ResponseEntity<byte[]> exportCsv() {
    byte[] bytes = adminService.exportCsv().getBytes(StandardCharsets.UTF_8);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"candidatures-douala-2026.csv\"")
        .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
        .body(bytes);
  }
}
