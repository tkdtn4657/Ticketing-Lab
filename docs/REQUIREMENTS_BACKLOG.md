# Requirements Backlog

상태 기준:
- `implemented`: 코드/테스트 기준으로 완료 확인
- `in_progress`: 일부 진행 중
- `planned`: 미구현

| Epic | Req ID | Name | Description / DoD | Priority | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| Common | REQ-COM-001 | JSON API 공통 포맷 | 모든 요청/응답이 JSON 기반이며 Content-Type 을 준수한다 | P0 | planned | |
| Common | REQ-COM-002 | 표준 에러 응답 | `{code,message,traceId}` 형태의 공통 에러 응답을 제공한다 | P0 | planned | |
| Common | REQ-COM-003 | 시간 포맷 통일 | ISO-8601, Asia/Seoul 기준으로 일관되게 반환한다 | P1 | planned | |
| Auth | REQ-AUTH-001 | 회원가입(로컬) | 이메일 unique, BCrypt 해시, 기본 USER role | P0 | implemented | |
| Auth | REQ-AUTH-002 | 로그인(로컬) | email/password 로 access/refresh 발급 | P0 | implemented | |
| Auth | REQ-AUTH-003 | JWT 인증 필터 | Bearer 토큰 검증 및 SecurityContext 세팅 | P0 | implemented | |
| Auth | REQ-AUTH-004 | Refresh 재발급 | refresh 로 access 재발급, 토큰 회전 처리 | P0 | implemented | rotation |
| Auth | REQ-AUTH-005 | 로그아웃 | refresh 토큰 폐기 처리 | P1 | implemented | DB revoke |
| Auth | REQ-AUTH-006 | 권한 분리 | USER/ADMIN API 접근 제어 | P0 | planned | ADMIN API 구현 시 구체화 |
| OAuth2 | REQ-OAUTH-001 | OAuth2 로그인 시작 | provider 별 authorize redirect 제공 | P0 | planned | Google/Kakao |
| OAuth2 | REQ-OAUTH-002 | OAuth2 콜백 처리 | code 교환, 사용자 조회/생성, JWT 발급 | P0 | planned | |
| OAuth2 | REQ-OAUTH-003 | 계정 연동 | 로그인 상태에서 소셜 계정 연결/해제 | P1 | planned | optional |
| View | REQ-VIEW-001 | 이벤트 목록/상세 조회 | 이벤트 목록/상세와 회차 조회 제공 | P0 | implemented | EVT-001, EVT-002 |
| View | REQ-VIEW-002 | 회차 가용성 조회 | 좌석 available, 구역 remaining 제공 | P0 | planned | SHW-001 |
| Hold | REQ-HOLD-001 | 선점 생성 | show 기준 좌석/구역 선점 생성 | P0 | planned | 혼합 금지 여부 결정 필요 |
| Hold | REQ-HOLD-002 | Hold TTL 만료 | 5분 TTL, 만료 자동 해제 | P0 | planned | scheduler/batch |
| Hold | REQ-HOLD-003 | 동시성 안전성 | 좌석 중복 선점 불가, 구역 잔여 음수 불가 | P0 | planned | 락 전략 필요 |
| Hold | REQ-HOLD-004 | 선점 조회/취소 | 본인 hold 조회 및 취소 | P0 | planned | |
| Reservation | REQ-RES-001 | 예약 생성 | hold_items 를 reservation_items 로 복사 | P0 | planned | |
| Reservation | REQ-RES-002 | 예약 상태 관리 | `PENDING_PAYMENT -> PAID`, 만료/취소 포함 | P0 | planned | |
| Payment | REQ-PAY-001 | 결제 멱등성 | `Idempotency-Key` 지원 | P0 | planned | |
| Payment | REQ-PAY-002 | 결제 승인 처리 | 승인 시 PAID 전환 및 티켓 발급 | P0 | planned | |
| Ticket | REQ-TKT-001 | 티켓 발급 | 결제 성공 시 ticket 생성 | P0 | planned | |
| Checkin | REQ-CHK-001 | 체크인(QR) | `qrToken` 검증 후 USED 처리 | P1 | planned | ADMIN |
| Admin | REQ-ADM-001 | Venue 업서트 | code 기반 upsert, venueId 반환 | P0 | planned | |
| Admin | REQ-ADM-004 | 이벤트/회차/판매단위 관리 | 이벤트, 회차, 좌석판매, 구역재고 생성 | P0 | planned | |