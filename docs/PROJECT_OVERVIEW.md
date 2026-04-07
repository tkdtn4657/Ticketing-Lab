# 프로젝트 개요

## 목표
- 공연/이벤트 티켓 예매 시스템 MVP를 구현한다.
- 모놀리식 Spring MVC 기반으로 시작한다.
- 핵심 플로우는 `Hold -> Reservation -> Payment -> Ticket -> Check-in` 이다.

## 판매 단위와 인벤토리
- 판매 단위는 `Show(회차)` 이다.
- 인벤토리는 `지정형 Seat` 우선이다.
- 확장 모델로 `Section 수량형` 도 지원한다.

## 데이터 모델

### 기준 정보
- `venues`
- `seats(venue_id)`
- `sections(venue_id)`
- `events`
- `shows(event_id, venue_id, start_at, status)`

### 인벤토리
- `show_seats(show_id, seat_id, price, status, version)`
- `show_section_inventories(show_id, section_id, price, capacity, sold_qty, hold_qty, version)`

### 홀드 / 예약
- `holds(id uuid, user_id, show_id, status, expires_at)`
- `hold_items(id, hold_id, type[SEAT/SECTION], seat_id?, section_id?, qty, unit_price)`
- `reservations(id uuid, user_id, show_id, status, total_amount, expires_at)`
- `reservation_items(id, reservation_id, type, seat_id?, section_id?, qty, unit_price)`

> `hold_items`를 분리하는 이유는 하나의 hold 안에 여러 좌석/구역이 들어가는 1:N 구조를 자연스럽게 표현하기 위해서다.

### 결제 / 티켓
- `payments(id, reservation_id, provider, idempotency_key, status, amount, approved_at, raw_payload)`
- `tickets(id uuid, reservation_item_id, serial, qr_token, status, used_at, created_at)`

## 상태 전이
- Hold: `ACTIVE -> CONVERTED | CANCELED | EXPIRED`
- Reservation: `PENDING_PAYMENT -> PAID | CANCELED | EXPIRED`
- Payment: `REQUESTED -> APPROVED | FAILED`
- Ticket: `ISSUED -> USED | CANCELED(optional)`

## 판매 준비 흐름
1. `Venue`를 생성하거나 수정한다.
2. 공연장 기준 좌석은 `POST /api/admin/venues/{venueId}/seats`로 등록한다.
3. 공연장 기준 구역은 `POST /api/admin/venues/{venueId}/sections`로 등록한다.
4. 필요하면 `GET /api/admin/venues/{venueId}/seats`, `GET /api/admin/venues/{venueId}/sections`로 기준정보 ID를 확인한다.
5. `Event`를 생성하고, 그 아래에 `Show`를 생성한다.
6. 회차별 판매 좌석은 `POST /api/admin/shows/{showId}/show-seats`로 생성한다.
7. 회차별 구역 재고는 `POST /api/admin/shows/{showId}/section-inventories`로 생성한다.
8. `GET /api/shows/{showId}/availability`는 위에서 생성된 `show_seats`, `show_section_inventories`를 읽는다.

### 좌석 데이터 관점 정리
- `seats`, `sections`는 공연장 기준정보다.
- `show_seats`, `show_section_inventories`는 회차별 판매 인벤토리다.
- 즉 좌석 마스터를 먼저 만들고, 실제 판매용 인벤토리는 회차 생성 뒤 별도로 붙인다.
- `SHW-001`는 좌석을 생성하지 않고, 이미 준비된 회차 인벤토리를 조회만 한다.

## MVP 범위

### 공개 조회
- `GET /api/events`
- `GET /api/events/{eventId}`
- `GET /api/shows/{showId}/availability`

### SHW-001 응답 요약
- `seats[]`: `seatId`, `label`, `rowNo`, `colNo`, `price`, `available`
- `sections[]`: `sectionId`, `name`, `price`, `remainingQty`

### Hold
- `POST /api/holds`
- `GET /api/holds/{holdId}`
- `DELETE /api/holds/{holdId}`
- 요청 아이템은 `seatId` 또는 `sectionId` 중 하나를 가진다.
- 좌석 홀드는 `qty=1` 고정이며, 구역 홀드는 `qty`를 반드시 전달한다.
- Hold TTL은 5분이며, 생성/조회/취소 시점에 만료된 hold를 자동 해제한다.

### Reservation
- `POST /api/reservations`
- `GET /api/reservations/{reservationId}`
- `GET /api/me/reservations`
- 예약 생성 시 hold는 `CONVERTED`로 바뀌고, 좌석은 `RESERVED`, 구역 수량은 기존 `hold_qty`를 유지한다.
- Reservation TTL은 15분이며, 생성/상세/목록/새 hold 생성 시점에 만료된 reservation을 lazy release 한다.

### Payment
- `POST /api/payments/confirm` with Header `Idempotency-Key`
- `reservationId`, `amount`는 reservation 소유권과 총액 기준으로 검증한다.
- 성공 시 reservation은 `PAID`가 되고, 구역형 재고는 `hold_qty`에서 `sold_qty`로 이동한다.
- 같은 `Idempotency-Key`로 같은 요청을 재호출하면 동일한 payment 응답을 반환한다.

### Ticket / Check-in
- `GET /api/me/tickets`
- 결제 성공 시 `reservation_items` 기준으로 ticket을 발급한다.
- 좌석형 item은 1장, 구역형 item은 `qty`만큼 개별 ticket이 생성된다.
- `POST /api/checkin`
- 체크인 API는 `ADMIN`만 호출할 수 있으며, `qrToken` 기준으로 ticket을 조회한다.
- 체크인 성공 시 ticket은 `ISSUED -> USED`로 전이되고 `used_at`이 기록된다.
- 이미 사용된 ticket을 다시 체크인하면 `409 Conflict`로 중복 입장을 막는다.
- 체크인 이후 `GET /api/me/tickets` 응답에는 `status=USED`, `usedAt`이 반영된다.

### Admin
- `POST /api/admin/venues/upsert`
- `POST /api/admin/venues/{venueId}/seats`
- `GET /api/admin/venues/{venueId}/seats`
- `POST /api/admin/venues/{venueId}/sections`
- `GET /api/admin/venues/{venueId}/sections`
- `POST /api/admin/events`
- `POST /api/admin/shows`
- `POST /api/admin/shows/{showId}/show-seats`
- `POST /api/admin/shows/{showId}/section-inventories`

## 단기 우선순위
1. Auth 회원가입, 로그인, 토큰 흐름 안정화
2. Hold/Reservation 동시성 처리 뼈대 구현
3. Admin venue/show inventory API 구현
4. 결제 승인, 티켓 발급, 체크인 연결
