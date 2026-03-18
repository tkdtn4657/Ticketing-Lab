# AGENTS.md - Ticketing-Lab-2

## Purpose
- This file is the working guide for coding agents.
- Keep this file short and high-signal.
- Detailed specs live under `docs/`.

## Project Snapshot
- 공연/이벤트 티켓 예매 시스템 MVP를 개발한다.
- 아키텍처는 모놀리식 Spring MVC 기반이다.
- 핵심 플로우는 `Hold -> Reservation -> Payment -> Ticket -> Check-in` 이다.
- 판매 단위는 `Show(회차)` 이다.
- 인벤토리는 `지정형 Seat` 우선이며, 확장으로 `Section 수량형`을 포함한다.

## Coding Rules
- 객체지향적인 코드를 지향한다.
- 패키지는 기능 기준으로 분리한다: `auth`, `user`, `venue`, `event`, `show`, `hold`, `reservation`, `payment`, `ticket`, `checkin`.
- 각 기능 내부는 기본적으로 `presentation`, `application`, `domain`, `infrastructure` 계층을 유지한다.
- Controller는 thin 하게 유지한다: 입력 검증, UseCase 호출, 응답 조립만 담당한다.
- UseCase는 유스케이스 단위로 분리하고, 가능하면 `execute()` 중심의 단일 진입점을 유지한다.
- domain 에는 행위와 규칙을 두고, Controller/Service가 비대해지지 않게 한다.
- 설명은 기본적으로 한글로 적는다.
- 각 코드별 주석을 이해하기 용이하도록 짧게 적는다.
- 커밋은 한글로 작성 ex) `feat: 기능 추가`
- 일관된 코딩 스타일을 적용한다.

## Implementation Conventions
- DTO는 Java `record` 사용 가능.
- JPA Entity 필드명은 camelCase, DB 컬럼은 `snake_case`로 매핑한다.
- Lombok 사용 시 `@RequiredArgsConstructor` 와 `@NoArgsConstructor` 중복으로 0-arg 생성자가 충돌하지 않게 주의한다.
- 텍스트 파일은 UTF-8 계열 인코딩을 사용한다.
- 문서 파일(`*.md`)은 `UTF-8 with BOM`을 사용한다.
- 소스/설정 파일(`*.java`, `*.yml`, `*.yaml`, `*.sql`, `*.js`, `*.css`, `*.html`, `*.properties`)은 `UTF-8 without BOM`을 사용한다.
- PowerShell로 파일을 읽거나 쓸 때는 기본 인코딩을 사용하지 말고 파일 종류에 맞는 인코딩을 명시한다.

## Security Rules
- Spring MVC 이므로 `SecurityFilterChain` 을 사용한다.
- API 테스트 편의를 위해 필요한 공개 조회 API만 `permitAll` 한다.
- API에서는 CSRF를 비활성화한다.
- 비밀번호 해싱은 BCrypt를 사용하고, 검증은 반드시 `passwordEncoder.matches()` 로 처리한다.
- 로그인 성공 시 Access Token은 Authorization 헤더, Refresh Token은 HttpOnly Cookie 또는 명시적 응답 DTO 정책에 맞춰 다룬다.

## Source of Truth Docs
- `docs/PROJECT_OVERVIEW.md`: 목표, 도메인 모델, 상태 전이, MVP 범위.
- `docs/API_CATALOG.md`: API 목록, 요청/응답 개요, 우선순위, 구현 상태.
- `docs/REQUIREMENTS_BACKLOG.md`: 요구사항 백로그와 완료 조건.

## Update Rules
- API 또는 상태가 바뀌면 관련 `docs/` 문서를 같은 변경에서 함께 갱신한다.
- 코드/테스트와 문서가 충돌하면 우선 코드와 테스트를 기준으로 보고, 문서를 즉시 따라오게 수정한다.
- 긴 표, 장문의 백로그, 세부 기획안은 이 파일에 직접 누적하지 않는다.
