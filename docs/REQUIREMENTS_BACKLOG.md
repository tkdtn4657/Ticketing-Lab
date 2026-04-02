# 요구사항 백로그

상태 기준:
- `구현완료`: 코드와 테스트 기준으로 완료 확인
- `진행중`: 일부 진행 중
- `예정`: 아직 미구현

| 에픽 | 요구사항 ID | 이름 | 설명 / 완료 조건 | 우선순위 | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| Common | REQ-COM-001 | JSON API 공통 형식 | 모든 요청/응답은 JSON 기반이며 Content-Type을 일관되게 사용한다 | P0 | 예정 | |
| Common | REQ-COM-002 | 공통 에러 응답 | `{code,message,traceId}` 형태의 공통 에러 응답을 제공한다 | P0 | 예정 | |
| Common | REQ-COM-003 | 시간 표현 일관성 | ISO-8601, Asia/Seoul 기준으로 일관되게 반환한다 | P1 | 예정 | |
| Auth | REQ-AUTH-001 | 회원가입 | 이메일 unique, BCrypt 해시, 기본 USER role | P0 | 구현완료 | |
| Auth | REQ-AUTH-002 | 로그인 | email/password로 access/refresh 발급 | P0 | 구현완료 | |
| Auth | REQ-AUTH-003 | JWT 인증 필터 | Bearer 토큰 검증 및 SecurityContext 세팅 | P0 | 구현완료 | |
| Auth | REQ-AUTH-004 | Refresh 재발급 | refresh로 access 재발급, 토큰 회전 처리 | P0 | 구현완료 | rotation |
| Auth | REQ-AUTH-005 | 로그아웃 | refresh 토큰 무효 처리 | P1 | 구현완료 | DB revoke |
| Auth | REQ-AUTH-006 | 권한 분리 | USER/ADMIN API 접근 제어 | P0 | 구현완료 | `/api/admin/**`, `/api/checkin` ADMIN 제한 |
| OAuth2 | REQ-OAUTH-001 | OAuth2 로그인 시작 | provider별 authorize redirect 제공 | P0 | 예정 | Google/Kakao |
| OAuth2 | REQ-OAUTH-002 | OAuth2 콜백 처리 | code 교환, 사용자 조회/생성, JWT 발급 | P0 | 예정 | |
| OAuth2 | REQ-OAUTH-003 | 계정 연결 | 로그인 상태에서 소셜 계정 연결/해제 | P1 | 예정 | optional |
| View | REQ-VIEW-001 | 이벤트 목록/상세 조회 | 이벤트 목록/상세와 회차 조회 제공 | P0 | 구현완료 | EVT-001, EVT-002 |
| View | REQ-VIEW-002 | 회차 가용성 조회 | 좌석 available, 구역 remainingQty 제공 | P0 | 구현완료 | SHW-001 |
| Hold | REQ-HOLD-001 | 홀드 생성 | show 기준 좌석/구역 홀드 생성 | P0 | 구현완료 | 좌석/구역 혼합 items 지원 |
| Hold | REQ-HOLD-002 | Hold TTL 만료 | 5분 TTL, 만료 자동 해제 | P0 | 진행중 | create/get/delete 시 lazy release, scheduler 미구현 |
| Hold | REQ-HOLD-003 | 동시성 안전성 | 좌석 중복 홀드 불가, 구역 잔여 수량 보호 | P0 | 진행중 | 요청 자원 기준 row lock 적용 |
| Hold | REQ-HOLD-004 | 홀드 조회/취소 | 본인 hold 조회 및 취소 | P0 | 구현완료 | 만료 조회 시 EXPIRED 반영 |
| Reservation | REQ-RES-001 | 예약 생성 | hold_items를 reservation_items로 복사 | P0 | 구현완료 | hold -> reservation 전환 및 자원 유지 |
| Reservation | REQ-RES-002 | 예약 상태 관리 | `PENDING_PAYMENT -> PAID`, 만료/취소 포함 | P0 | 진행중 | `EXPIRED` lazy release 구현, `PAID/CANCELED`는 후속 단계 |
| Payment | REQ-PAY-001 | 결제 멱등성 | `Idempotency-Key` 지원 | P0 | 예정 | |
| Payment | REQ-PAY-002 | 결제 승인 처리 | 승인 후 PAID 전환 및 티켓 발급 | P0 | 예정 | |
| Ticket | REQ-TKT-001 | 티켓 발급 | 결제 성공 후 ticket 생성 | P0 | 예정 | |
| Checkin | REQ-CHK-001 | 체크인 | `qrToken` 검증 및 USED 처리 | P1 | 예정 | ADMIN |
| Admin | REQ-ADM-001 | Venue 등록/수정 | code 기반 upsert, venueId 반환 | P0 | 구현완료 | |
| Admin | REQ-ADM-002 | 공연장 좌석/구역 기준정보 등록 | venue 기준 `seats`, `sections`를 생성한다 | P0 | 구현완료 | SHW-001 선행 |
| Admin | REQ-ADM-003 | 회차 판매 인벤토리 생성 | show 기준 `show_seats`, `show_section_inventories`를 생성한다 | P0 | 구현완료 | SHW-001 선행 |
| Admin | REQ-ADM-004 | 이벤트/회차/판매단위 관리 | 이벤트, 회차, 좌석판매, 구역재고 생성 | P0 | 구현완료 | |
| Admin | REQ-ADM-005 | 공연장 기준정보 조회 | venue 기준 `seats`, `sections`를 조회해 seatId/sectionId를 확인한다 | P1 | 구현완료 | admin console 지원 |
