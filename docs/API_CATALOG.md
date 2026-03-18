# API Catalog

상태 기준:
- `implemented`: 코드와 테스트 또는 동작 가능한 UI 기준으로 확인됨
- `in_progress`: 일부 구현 중
- `planned`: 아직 미구현

| Domain | ID | Method | Path | Auth | Purpose | Request | Response | Priority | Status |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Auth | AUTH-001 | POST | `/api/auth/signup` | None | 로컬 회원가입 | `email`, `password` | `userId` | P0 | implemented |
| Auth | AUTH-002 | POST | `/api/auth/login` | None | 로컬 로그인, JWT 발급 | `email`, `password` | `accessToken`, `refreshToken` | P0 | implemented |
| Auth | AUTH-003 | POST | `/api/auth/refresh` | None | 토큰 재발급 | `refreshToken` | `accessToken`, `refreshToken` | P0 | implemented |
| Auth | AUTH-004 | POST | `/api/auth/logout` | USER | 로그아웃 | `refreshToken` | `204` | P1 | implemented |
| Auth | AUTH-005 | GET | `/api/auth/me` | USER | 내 정보 조회 | - | `userId`, `email`, `role` | P1 | implemented |
| OAuth2 | OAUTH-001 | GET | `/api/oauth2/authorize/{provider}` | None | OAuth2 로그인 시작 | `provider` | `302 redirect` | P0 | planned |
| OAuth2 | OAUTH-002 | GET | `/api/oauth2/callback/{provider}` | None | OAuth2 콜백, JWT 발급 | `code`, `state` | `accessToken`, `refreshToken` | P0 | planned |
| OAuth2 | OAUTH-003 | POST | `/api/oauth2/link/{provider}` | USER | 계정 연동 | `code/state or token` | `linked=true` | P1 | planned |
| OAuth2 | OAUTH-004 | DELETE | `/api/oauth2/link/{provider}` | USER | 계정 해제 | - | `204` | P1 | planned |
| Events | EVT-001 | GET | `/api/events` | None | 이벤트 목록 | `status?` | `events[]` | P0 | implemented |
| Events | EVT-002 | GET | `/api/events/{eventId}` | None | 이벤트 상세 + 회차 | - | `event`, `shows[]` | P0 | implemented |
| Shows | SHW-001 | GET | `/api/shows/{showId}/availability` | None | 좌석/구역 가용성 조회 | - | `seats[]`, `sections[]` | P0 | planned |
| Holds | HLD-001 | POST | `/api/holds` | USER | 선점 생성 | `showId`, `items[]` | `holdId`, `expiresAt` | P0 | planned |
| Holds | HLD-002 | GET | `/api/holds/{holdId}` | USER | 선점 조회 | - | `hold`, `items` | P0 | planned |
| Holds | HLD-003 | DELETE | `/api/holds/{holdId}` | USER | 선점 취소 | - | `204` | P0 | planned |
| Reservations | RES-001 | POST | `/api/reservations` | USER | 예약 생성 | `holdId` | `reservationId`, `status` | P0 | planned |
| Reservations | RES-002 | GET | `/api/reservations/{reservationId}` | USER | 예약 상세 | - | `reservation`, `items` | P1 | planned |
| Reservations | RES-003 | GET | `/api/me/reservations` | USER | 내 예약 목록 | `page`, `size`, `status?` | `paged list` | P1 | planned |
| Payments | PAY-001 | POST | `/api/payments/confirm` | USER | 결제 승인, 멱등 처리 | `reservationId`, `amount` | `paymentId`, `status` | P0 | planned |
| Tickets | TKT-001 | GET | `/api/me/tickets` | USER | 내 티켓 목록 | `page`, `size` | `paged tickets` | P1 | planned |
| Checkin | CHK-001 | POST | `/api/checkin` | ADMIN | 체크인 | `qrToken` | `USED` | P1 | planned |
| Admin | ADM-001 | POST | `/api/admin/venues/upsert` | ADMIN | venue 업서트 | `code`, `name`, `address` | `venueId` | P0 | planned |
| Admin | ADM-004 | POST | `/api/admin/events` | ADMIN | 이벤트 생성 | `title`, `desc`, `status` | `eventId` | P0 | planned |
| Admin | ADM-005 | POST | `/api/admin/shows` | ADMIN | 회차 생성 | `eventId`, `venueId`, `startAt` | `showId` | P0 | planned |