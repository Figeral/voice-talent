package com.voicetalent.backend.repository;

import com.voicetalent.backend.domain.ConsentRecord;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {}
