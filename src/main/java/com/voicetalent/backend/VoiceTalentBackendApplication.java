package com.voicetalent.backend;

import com.voicetalent.backend.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class VoiceTalentBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(VoiceTalentBackendApplication.class, args);
  }
}
