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