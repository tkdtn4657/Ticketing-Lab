# Project Overview

## Goal
- 공연/이벤트 티켓 예매 시스템 MVP를 구현한다.
- 모놀리식 Spring MVC 기반으로 시작한다.
- 핵심 플로우는 `Hold -> Reservation -> Payment -> Ticket -> Check-in` 이다.

## Sales Unit and Inventory
- 판매 단위는 `Show(회차)` 이다.
- 인벤토리는 `지정형 Seat` 우선이다.
- 확장 모델로 `Section 수량형` 을 지원한다.

## Data Model

### Master / Catalog
- `venues`
- `seats(venue_id)`
- `sections(venue_id)`
- `events`
- `shows(event_id, venue_id, start_at, status)`

### Inventory
- `show_seats(show_id, seat_id, price, status, version)`
- `show_section_inventories(show_id, section_id, price, capacity, sold_qty, hold_qty, version)`

### Hold / Reservation
- `holds(id uuid, user_id, show_id, status, expires_at)`
- `hold_items(id, hold_id, type[SEAT/SECTION], seat_id?, section_id?, qty, unit_price)`
- `reservations(id uuid, user_id, show_id, status, total_amount, expires_at)`
- `reservation_items(id, reservation_id, type, seat_id?, section_id?, qty, unit_price)`

> `hold_items` 를 분리하는 이유는 한 hold 에 여러 좌석/구역을 담는 1:N 라인아이템 구조를 유지하기 위해서다.

### Payment / Ticket
- `payments(id, reservation_id, provider, idempotency_key, status, amount, approved_at, raw_payload)`
- `tickets(id uuid, reservation_item_id, serial, qr_token, status, used_at, created_at)`

## State Transitions
- Hold: `ACTIVE -> CONVERTED | CANCELED | EXPIRED`
- Reservation: `PENDING_PAYMENT -> PAID | CANCELED | EXPIRED`
- Payment: `REQUESTED -> APPROVED | FAILED`
- Ticket: `ISSUED -> USED | CANCELED(optional)`

## MVP Scope

### Public View
- `GET /api/events`
- `GET /api/events/{eventId}`
- `GET /api/shows/{showId}/availability`

### Hold
- `POST /api/holds`
- `GET /api/holds/{holdId}`
- `DELETE /api/holds/{holdId}`

### Reservation
- `POST /api/reservations`
- `GET /api/reservations/{reservationId}`
- `GET /api/me/reservations`

### Payment
- `POST /api/payments/confirm` with `Idempotency-Key`

### Ticket / Check-in
- `GET /api/me/tickets`
- `POST /api/checkin`

### Admin
- `POST /api/admin/venues/upsert`
- `POST /api/admin/venues/{venueId}/seats`
- `POST /api/admin/venues/{venueId}/sections`
- `POST /api/admin/events`
- `POST /api/admin/shows`
- `POST /api/admin/shows/{showId}/show-seats`
- `POST /api/admin/shows/{showId}/section-inventories`

## Near-term Priorities
1. Auth 로컬 회원가입/로그인/토큰 흐름 안정화
2. Hold/Reservation 동시성 처리 설계
3. Show availability API 구현
4. 결제 승인과 티켓 발급 연결