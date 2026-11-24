package com.voicetalent.backend.repository;

import com.voicetalent.backend.domain.CandidateApplication;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateApplicationRepository extends JpaRepository<CandidateApplication, UUID> {
  boolean existsByPhone(String phone);
  boolean existsByEmail(String email);
  Optional<CandidateApplication> findByDossierNoAndPhone(String dossierNo, String phone);
  List<CandidateApplication> findAllByOrderBySubmittedAtDesc();
}
