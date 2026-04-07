package com.ticketinglab.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI ticketingLabOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ticketing Lab API")
                        .version("v1")
                        .description("구현 완료된 티켓 예매 MVP API 문서입니다. 통합 테스트와 테스트 페이지에서 확인한 요청/응답 예시를 기준으로 정리했습니다."))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("Authorization 헤더에 `Bearer {accessToken}` 형식으로 전달합니다.")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .tags(List.of(
                        new Tag().name("Admin").description("공연장, 이벤트, 회차, 판매 인벤토리 관리 API"),
                        new Tag().name("Auth").description("회원가입, 로그인, 토큰 재발급, 로그아웃, 내 정보 조회 API"),
                        new Tag().name("Checkin").description("관리자 체크인 처리 API"),
                        new Tag().name("Event").description("이벤트 목록 및 상세 조회 API"),
                        new Tag().name("Hold").description("좌석/구역 임시 선점 API"),
                        new Tag().name("Payment").description("예약 결제 승인 API"),
                        new Tag().name("Reservation").description("홀드 전환 및 내 예약 조회 API"),
                        new Tag().name("Show").description("회차별 좌석/구역 가용성 조회 API"),
                        new Tag().name("Ticket").description("내 티켓 조회 API")
                ));
    }
}