package com.ticketinglab.auth.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketinglab.auth.presentation.dto.LoginRequest;
import com.ticketinglab.auth.presentation.dto.RefreshTokenRequest;
import com.ticketinglab.auth.presentation.dto.SignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("AUTH-001 POST /api/auth/signup returns userId")
    void auth001_signup_returnsUserId() throws Exception {
        Credentials credentials = newCredentials();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new SignupRequest(credentials.email(), credentials.password()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber());
    }

    @Test
    @DisplayName("AUTH-001 duplicate signup returns 409 conflict")
    void auth001_duplicateSignup_returnsConflict() throws Exception {
        Credentials credentials = newCredentials();
        signup(credentials);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new SignupRequest(credentials.email(), credentials.password()))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("AUTH-002 POST /api/auth/login returns accessToken and refreshToken")
    void auth002_login_returnsAccessTokenAndRefreshToken() throws Exception {
        Credentials credentials = newCredentials();
        signup(credentials);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest(credentials.email(), credentials.password()))))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", startsWith("Bearer ")))
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString());
    }

    @Test
    @DisplayName("AUTH-002 login replaces previous token session")
    void auth002_login_replacesPreviousTokenSession() throws Exception {
        Credentials credentials = newCredentials();
        Long userId = signup(credentials);

        TokenBundle firstTokens = login(credentials);
        TokenBundle secondTokens = login(credentials);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshTokenRequest(firstTokens.refreshToken()))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + firstTokens.accessToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + secondTokens.accessToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    @DisplayName("AUTH-003 POST /api/auth/refresh rotates refreshToken and returns new tokens")
    void auth003_refresh_rotatesRefreshToken() throws Exception {
        AuthSession session = signupAndLogin();

        AuthSession refreshedSession = refresh(session.refreshToken());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshTokenRequest(session.refreshToken()))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshTokenRequest(refreshedSession.refreshToken()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString());
    }

    @Test
    @DisplayName("AUTH-004 POST /api/auth/logout invalidates refreshToken and returns 204")
    void auth004_logout_invalidatesRefreshToken() throws Exception {
        AuthSession session = signupAndLogin();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + session.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshTokenRequest(session.refreshToken()))))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", startsWith("refresh-token=;")));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshTokenRequest(session.refreshToken()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-005 GET /api/auth/me returns userId email role")
    void auth005_me_returnsUserInfo() throws Exception {
        AuthSession session = signupAndLogin();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + session.accessToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(session.userId()))
                .andExpect(jsonPath("$.email").value(session.email()))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    private AuthSession signupAndLogin() throws Exception {
        Credentials credentials = newCredentials();
        Long userId = signup(credentials);
        TokenBundle tokens = login(credentials);

        return new AuthSession(userId, credentials.email(), tokens.accessToken(), tokens.refreshToken());
    }

    private Long signup(Credentials credentials) throws Exception {
        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new SignupRequest(credentials.email(), credentials.password()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andReturn();

        JsonNode signupBody = objectMapper.readTree(signupResult.getResponse().getContentAsString());
        return signupBody.get("userId").asLong();
    }

    private TokenBundle login(Credentials credentials) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest(credentials.email(), credentials.password()))))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", startsWith("Bearer ")))
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return new TokenBundle(
                loginBody.get("accessToken").asText(),
                loginBody.get("refreshToken").asText()
        );
    }

    private AuthSession refresh(String refreshToken) throws Exception {
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", startsWith("Bearer ")))
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn();

        JsonNode refreshBody = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        return new AuthSession(
                null,
                null,
                refreshBody.get("accessToken").asText(),
                refreshBody.get("refreshToken").asText()
        );
    }

    private Credentials newCredentials() {
        String email = "user" + System.nanoTime() + "@example.com";
        return new Credentials(email, "password123");
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private record Credentials(String email, String password) {
    }

    private record TokenBundle(String accessToken, String refreshToken) {
    }

    private record AuthSession(
            Long userId,
            String email,
            String accessToken,
            String refreshToken
    ) {
    }
}
