package com.voicetalent.backend.repository;

import com.voicetalent.backend.domain.MediaUpload;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaUploadRepository extends JpaRepository<MediaUpload, UUID> {}
