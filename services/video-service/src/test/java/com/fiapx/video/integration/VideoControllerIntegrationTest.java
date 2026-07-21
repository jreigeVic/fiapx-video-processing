package com.fiapx.video.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fiapx.video.application.ports.out.EventPublisherPort;
import com.fiapx.video.application.ports.out.StoragePort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VideoControllerIntegrationTest {

    private static final String JWT_SECRET = "CHANGE_ME_DEV_ONLY_SECRET_KEY_MIN_32_BYTES_LONG_0000";

    @Autowired private MockMvc mockMvc;

    @MockBean private StoragePort storagePort;

    @MockBean private EventPublisherPort eventPublisherPort;

    @Test
    void listWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/videos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void uploadListAndGetVideo() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = mintAccessToken(userId, "owner@user.com");

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "movie.mp4",
                        "video/mp4",
                        "fake-bytes".getBytes(StandardCharsets.UTF_8));

        when(storagePort.generatePresignedDownloadUrl(any(), any()))
                .thenReturn("https://example.com/signed");

        String uploadJson =
                mockMvc.perform(
                                multipart("/api/videos")
                                        .file(file)
                                        .header("Authorization", "Bearer " + token))
                        .andExpect(status().isAccepted())
                        .andExpect(jsonPath("$.status").value("PROCESSING"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String videoId =
                com.fasterxml.jackson.databind.json.JsonMapper.builder()
                        .build()
                        .readTree(uploadJson)
                        .get("videoId")
                        .asText();

        mockMvc.perform(get("/api/videos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(videoId));

        mockMvc.perform(get("/api/videos/" + videoId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.downloadAvailable").value(false));
    }

    @Test
    void getVideoOwnedByAnotherUserIsNotFound() throws Exception {
        String token = mintAccessToken(UUID.randomUUID(), "owner@user.com");

        mockMvc.perform(
                        get("/api/videos/" + UUID.randomUUID())
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private String mintAccessToken(UUID userId, String email) {
        Key signingKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(Duration.ofMinutes(15))))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
