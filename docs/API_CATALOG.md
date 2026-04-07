# API 카탈로그

## Swagger / OpenAPI
- Swagger UI: `/docs/swagger-ui.html`
- OpenAPI JSON: `/docs/api-docs`
- OpenAPI YAML: `/docs/api-docs.yaml`
- Swagger 문서는 `/api/**`만 노출하며 Bearer 인증을 기본으로 적용한다.
- 공개 API는 `POST /api/auth/signup`, `POST /api/auth/login`, `POST /api/auth/refresh`, `GET /api/events`, `GET /api/events/{eventId}`, `GET /api/shows/{showId}/availability` 이다.
- 로그인/토큰 재발급 응답은 `Authorization: Bearer ...` 헤더와 `refresh-token` HttpOnly Cookie를 함께 내려준다.
- 결제 승인 API는 `Idempotency-Key` 헤더가 반드시 필요하다.
- Swagger 예시는 통합 테스트와 정적 테스트 페이지(`auth-test.html`, `events-test.html`, `shows-test.html`, `holds-test.html`, `reservations-test.html`, `payments-test.html`, `checkin-test.html`, `admin-test.html`)를 기준으로 정리했다.

상태 기준:
- `구현완료`: 코드와 테스트, 또는 동작 가능한 UI 기준으로 확인 완료
- `진행중`: 일부 구현 중
- `예정`: 아직 미구현

| 도메인 | ID | 메서드 | 경로 | 인증 | 목적 | 요청 | 응답 | 우선순위 | 상태 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Auth | AUTH-001 | POST | `/api/auth/signup` | 없음 | 회원가입 | `email`, `password` | `userId` | P0 | 구현완료 |
| Auth | AUTH-002 | POST | `/api/auth/login` | 없음 | 로그인 및 JWT 발급 | `email`, `password` | `Authorization 헤더`, `accessToken`, `refreshToken`, `refresh-token Cookie` | P0 | 구현완료 |
| Auth | AUTH-003 | POST | `/api/auth/refresh` | 없음 | 토큰 재발급 | `refreshToken` | `Authorization 헤더`, `accessToken`, `refreshToken`, `refresh-token Cookie` | P0 | 구현완료 |
| Auth | AUTH-004 | POST | `/api/auth/logout` | USER | 로그아웃 | `refreshToken` | `204`, `refresh-token Cookie 삭제` | P1 | 구현완료 |
| Auth | AUTH-005 | GET | `/api/auth/me` | USER | 내 정보 조회 | - | `userId`, `email`, `role` | P1 | 구현완료 |
| OAuth2 | OAUTH-001 | GET | `/api/oauth2/authorize/{provider}` | 없음 | OAuth2 로그인 시작 | `provider` | `302 redirect` | P0 | 예정 |
| OAuth2 | OAUTH-002 | GET | `/api/oauth2/callback/{provider}` | 없음 | OAuth2 콜백 및 JWT 발급 | `code`, `state` | `accessToken`, `refreshToken` | P0 | 예정 |
| OAuth2 | OAUTH-003 | POST | `/api/oauth2/link/{provider}` | USER | 계정 연결 | `code/state or token` | `linked=true` | P1 | 예정 |
| OAuth2 | OAUTH-004 | DELETE | `/api/oauth2/link/{provider}` | USER | 계정 연결 해제 | - | `204` | P1 | 예정 |
| Events | EVT-001 | GET | `/api/events` | 없음 | 이벤트 목록 조회 | `status?` | `events[]` | P0 | 구현완료 |
| Events | EVT-002 | GET | `/api/events/{eventId}` | 없음 | 이벤트 상세 및 회차 조회 | - | `event`, `shows[]` | P0 | 구현완료 |
| Shows | SHW-001 | GET | `/api/shows/{showId}/availability` | 없음 | 좌석/구역 가용성 조회 | - | `seats[seatId,label,rowNo,colNo,price,available]`, `sections[sectionId,name,price,remainingQty]` | P0 | 구현완료 |
| Holds | HLD-001 | POST | `/api/holds` | USER | 홀드 생성 | `showId`, `items[seatId?,sectionId?,qty?]` | `holdId`, `expiresAt` | P0 | 구현완료 |
| Holds | HLD-002 | GET | `/api/holds/{holdId}` | USER | 홀드 조회 | - | `hold[holdId,showId,status,expiresAt,createdAt]`, `items[type,seatId,sectionId,qty,unitPrice]` | P0 | 구현완료 |
| Holds | HLD-003 | DELETE | `/api/holds/{holdId}` | USER | 홀드 취소 | - | `204` | P0 | 구현완료 |
| Reservations | RES-001 | POST | `/api/reservations` | USER | 홀드를 결제 대기 예약으로 전환 | `holdId` | `reservationId`, `status` | P0 | 구현완료 |
| Reservations | RES-002 | GET | `/api/reservations/{reservationId}` | USER | 예약 상세 조회 | - | `reservation[reservationId,showId,status,totalAmount,expiresAt,createdAt]`, `items[type,seatId,sectionId,qty,unitPrice]` | P1 | 구현완료 |
| Reservations | RES-003 | GET | `/api/me/reservations` | USER | 내 예약 목록 조회 | `page`, `size`, `status?` | `page,size,totalElements,totalPages,reservations[]` | P1 | 구현완료 |
| Payments | PAY-001 | POST | `/api/payments/confirm` | USER | 결제 승인 및 멱등 처리 | Header `Idempotency-Key`, Body `reservationId`, `amount` | `paymentId`, `reservationId`, `status`, `reservationStatus`, `approvedAt` | P0 | 구현완료 |
| Tickets | TKT-001 | GET | `/api/me/tickets` | USER | 내 티켓 목록 조회 | `page`, `size` | `page,size,totalElements,totalPages,tickets[ticketId,reservationId,showId,reservationItemId,type,seatId,sectionId,serial,qrToken,status,usedAt,createdAt]` | P1 | 구현완료 |
| Checkin | CHK-001 | POST | `/api/checkin` | ADMIN | 체크인 처리 | `qrToken` | `ticketId,reservationId,showId,reservationItemId,type,seatId,sectionId,serial,qrToken,status,usedAt` | P1 | 구현완료 |
| Admin | ADM-001 | POST | `/api/admin/venues/upsert` | ADMIN | 공연장 등록/수정 | `code`, `name`, `address` | `venueId` | P0 | 구현완료 |
| Admin | ADM-002 | POST | `/api/admin/venues/{venueId}/seats` | ADMIN | 공연장 좌석 기준정보 등록 | `seats[]` | `createdCount` | P0 | 구현완료 |
| Admin | ADM-008 | GET | `/api/admin/venues/{venueId}/seats` | ADMIN | 공연장 좌석 기준정보 조회 | - | `seats[seatId,label,rowNo,colNo]` | P1 | 구현완료 |
| Admin | ADM-003 | POST | `/api/admin/venues/{venueId}/sections` | ADMIN | 공연장 구역 기준정보 등록 | `sections[]` | `createdCount` | P0 | 구현완료 |
| Admin | ADM-009 | GET | `/api/admin/venues/{venueId}/sections` | ADMIN | 공연장 구역 기준정보 조회 | - | `sections[sectionId,name]` | P1 | 구현완료 |
| Admin | ADM-004 | POST | `/api/admin/events` | ADMIN | 이벤트 생성 | `title`, `desc`, `status` | `eventId` | P0 | 구현완료 |
| Admin | ADM-005 | POST | `/api/admin/shows` | ADMIN | 회차 생성 | `eventId`, `venueId`, `startAt` | `showId` | P0 | 구현완료 |
| Admin | ADM-006 | POST | `/api/admin/shows/{showId}/show-seats` | ADMIN | 회차 좌석 판매 정보 생성 | `items[seatId,price]` | `createdCount` | P0 | 구현완료 |
| Admin | ADM-007 | POST | `/api/admin/shows/{showId}/section-inventories` | ADMIN | 회차 구역 재고 생성 | `items[sectionId,price,capacity]` | `createdCount` | P0 | 구현완료 |