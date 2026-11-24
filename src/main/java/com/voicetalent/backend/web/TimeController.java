package com.voicetalent.backend.web;

import com.voicetalent.backend.web.dto.TimeResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Time", description = "Server clock synchronization for countdowns and registration window")
public class TimeController {

  private final Clock clock;

  public TimeController(Clock clock) {
    this.clock = clock;
  }

  @GetMapping("/api/time")
  public ResponseEntity<TimeResponse> getTime() {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(new TimeResponse(clock.millis()));
  }
}
