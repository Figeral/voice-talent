package com.voicetalent.backend;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationFlowIntegrationTest {

  @TempDir
  static Path tempUploadsDir;

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("app.storage.local-dir", () -> tempUploadsDir.toString());
  }

  @TestConfiguration
  static class FixedClockConfig {
    @Bean
    @Primary
    Clock testClock() {
      return Clock.fixed(Instant.parse("2026-11-26T12:00:00Z"), ZoneOffset.UTC);
    }
  }

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  void timeEndpointReturnsServerEpoch() throws Exception {
    mockMvc.perform(get("/api/time"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.now").value(Instant.parse("2026-11-26T12:00:00Z").toEpochMilli()));
  }

  @Test
  void openApiDocsAreExposed() throws Exception {
    mockMvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("/api/registration")));
  }

  @Test
  void uploadValidationRejectsBadMimeAndDuration() throws Exception {
    mockMvc.perform(post("/api/registration/uploads")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"kind":"photo","mime":"application/pdf","size":1024}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("Format")));

    mockMvc.perform(post("/api/registration/uploads")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"kind":"presentation","mime":"video/mp4","size":2048,"durationSeconds":8}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("courte")));
  }

  @Test
  void fullRegistrationStatusAndAdminWorkflow() throws Exception {
    // 1. Prepare and upload photo (using FormData empty field name "" like frontend)
    MvcResult photoPrep = mockMvc.perform(post("/api/registration/uploads")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"kind":"photo","mime":"image/webp","size":1024}
                """))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode photoJson = objectMapper.readTree(photoPrep.getResponse().getContentAsString());
    String photoId = photoJson.get("uploadId").asString();
    String photoSignedUrl = photoJson.get("signedUrl").asString().replace("http://localhost:8080", "");

    MockMultipartFile photoFile = new MockMultipartFile("file", "portrait.webp", "image/webp", "webp-bytes".getBytes(StandardCharsets.UTF_8));
    mockMvc.perform(multipart(photoSignedUrl)
            .file(photoFile)
            .param("cacheControl", "3600")
            .with(req -> { req.setMethod("PUT"); return req; }))
        .andExpect(status().isOk());

    // 2. Prepare and upload video
    MvcResult videoPrep = mockMvc.perform(post("/api/registration/uploads")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"kind":"presentation","mime":"video/mp4","size":4096,"durationSeconds":22}
                """))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode videoJson = objectMapper.readTree(videoPrep.getResponse().getContentAsString());
    String videoId = videoJson.get("uploadId").asString();
    String videoSignedUrl = videoJson.get("signedUrl").asString().replace("http://localhost:8080", "");

    MockMultipartFile videoFile = new MockMultipartFile("file", "audition.mp4", "video/mp4", "mp4-bytes".getBytes(StandardCharsets.UTF_8));
    mockMvc.perform(multipart(videoSignedUrl)
            .file(videoFile)
            .param("cacheControl", "3600")
            .with(req -> { req.setMethod("PUT"); return req; }))
        .andExpect(status().isOk());

    // 3. Submit registration
    String body = objectMapper.writeValueAsString(Map.ofEntries(
        Map.entry("lastName", "Mbarga"),
        Map.entry("firstName", "Amina"),
        Map.entry("stageName", "Amina V"),
        Map.entry("birthDate", "2001-05-14"),
        Map.entry("gender", "female"),
        Map.entry("phone", "6 71 23 45 67"),
        Map.entry("email", "Amina@Example.com"),
        Map.entry("city", "Douala"),
        Map.entry("district", "Bonapriso"),
        Map.entry("contactName", "Paul Mbarga"),
        Map.entry("contactPhone", "699001122"),
        Map.entry("photoUploadId", photoId),
        Map.entry("musicGenre", "Afrobeat"),
        Map.entry("songTitle", "Lumière"),
        Map.entry("musicBackground", "5 ans de chorale à Douala."),
        Map.entry("socialLinks", Map.of("instagram", "https://instagram.com/aminav")),
        Map.entry("presentationUploadId", videoId),
        Map.entry("acceptRules", true),
        Map.entry("website", "")
    ));

    MvcResult regRes = mockMvc.perform(post("/api/registration")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.dossierNo", is("VT-2026-0001")))
        .andReturn();

    String dossierNo = objectMapper.readTree(regRes.getResponse().getContentAsString()).get("dossierNo").asString();

    // 4. Duplicate phone/email rejected with 409 + fieldErrors
    mockMvc.perform(post("/api/registration")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.fieldErrors.phone", notNullValue()))
        .andExpect(jsonPath("$.fieldErrors.email", notNullValue()));

    // 5. Status lookup works with normalized phone
    mockMvc.perform(post("/api/registration/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"dossierNo":"vt-2026-0001","phone":"+237 671 23 45 67"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.dossierNo", is(dossierNo)))
        .andExpect(jsonPath("$.status", is("received")))
        .andExpect(jsonPath("$.firstName", is("Amina")));

    // 6. Admin endpoints require auth
    mockMvc.perform(get("/api/admin/applications"))
        .andExpect(status().isUnauthorized());

    MvcResult listRes = mockMvc.perform(get("/api/admin/applications")
            .with(httpBasic("admin", "changeme123")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].dossierNo", is(dossierNo)))
        .andReturn();

    String appId = objectMapper.readTree(listRes.getResponse().getContentAsString()).get(0).get("id").asString();

    // 7. Admin updates status & adds jury review
    mockMvc.perform(patch("/api/admin/applications/" + appId + "/status")
            .with(httpBasic("admin", "changeme123"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"status":"shortlisted","comment":"Très belle voix"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("shortlisted")));

    mockMvc.perform(post("/api/admin/applications/" + appId + "/reviews")
            .with(httpBasic("admin", "changeme123"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"score":88,"evaluationNotes":"Justesse et présence"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score", is(88)));

    mockMvc.perform(get("/api/admin/applications/" + appId)
            .with(httpBasic("admin", "changeme123")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reviews[0].score", is(88)))
        .andExpect(jsonPath("$.comments[0].commentText", is("Très belle voix")))
        .andExpect(jsonPath("$.statusHistory.length()", is(2)));

    mockMvc.perform(get("/api/admin/applications/export.csv")
            .with(httpBasic("admin", "changeme123")))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("VT-2026-0001")));
  }
}
