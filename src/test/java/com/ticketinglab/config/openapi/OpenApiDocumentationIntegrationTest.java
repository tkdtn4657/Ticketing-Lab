package com.ticketinglab.config.openapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("OpenAPI JSON exposes configured title, security scheme and representative paths")
    void openApiJson_containsConfiguredMetadata() throws Exception {
        mockMvc.perform(get("/docs/api-docs")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Ticketing Lab API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.tags[0]").value("Auth"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.summary").value("로그인"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.requestBody.description").value("로그인 요청"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.responses['200'].headers.Authorization.description")
                        .value("Bearer Access Token"))
                .andExpect(jsonPath("$.paths['/api/admin/venues/upsert'].post.summary").value("공연장 등록/수정"))
                .andExpect(jsonPath("$.paths['/api/master/venues'].get.summary").value("전체 공연장 목록 조회"))
                .andExpect(jsonPath("$.paths['/api/events'].get.summary").value("이벤트 목록 조회"))
                .andExpect(jsonPath("$.paths['/api/shows/{showId}/availability'].get.summary").value("회차 가용성 조회"))
                .andExpect(jsonPath("$.paths['/api/holds'].post.summary").value("홀드 생성"))
                .andExpect(jsonPath("$.paths['/api/reservations'].post.summary").value("예약 생성"))
                .andExpect(jsonPath("$.paths['/api/payments/confirm'].post.summary").value("결제 승인"))
                .andExpect(jsonPath("$.paths['/api/me/tickets'].get.summary").value("내 티켓 목록 조회"))
                .andExpect(jsonPath("$.paths['/api/checkin'].post.summary").value("체크인 처리"));
    }

    @Test
    @DisplayName("Configured Swagger UI path and YAML endpoint are accessible")
    void swaggerUiAndYamlEndpoints_areAccessible() throws Exception {
        MvcResult swaggerUiResult = mockMvc.perform(get("/docs/swagger-ui.html"))
                .andReturn();

        assertThat(swaggerUiResult.getResponse().getStatus()).isIn(200, 302);
        assertThat(swaggerUiResult.getResponse().getContentAsString() + swaggerUiResult.getResponse().getRedirectedUrl())
                .contains("swagger-ui");

        mockMvc.perform(get("/docs/swagger-ui/index.html"))
                .andExpect(status().isOk());

        MvcResult yamlResult = mockMvc.perform(get("/docs/api-docs.yaml"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(yamlResult.getResponse().getContentAsString())
                .contains("openapi:")
                .contains("/api/auth/login:");
    }
}
