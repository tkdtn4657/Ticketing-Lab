# 일반 기능 테스트 시나리오

## 문서 목적

- 백엔드의 일반 기능이 의도한 대로 동작하는지 확인하기 위한 시나리오를 정리한다.
- 성능 테스트 이전에 어떤 기능을 먼저 검증해야 하는지 기준을 만든다.
- 통합 테스트, 수동 테스트, 정적 테스트 페이지 확인 시 공통 체크리스트로 사용한다.

## 기본 원칙

이 문서는 `성능`이 아니라 `기능 정합성`을 확인하는 데 목적이 있다.

즉 아래 질문에 답할 수 있어야 한다.

- 요청이 정상적으로 처리되는가
- 권한이 없는 사용자는 차단되는가
- 잘못된 요청은 예측 가능한 에러로 처리되는가
- 상태 전이가 문서와 코드 의도대로 일어나는가
- 후속 API에서 이전 상태 변화가 올바르게 반영되는가

## main 브랜치 검증 결과

- 기준 브랜치:
  `backend` 로컬 `main`
- 검증 명령:
  `./gradlew.bat -g .gradle-home test --rerun-tasks`
- 검증 결과:
  `BUILD SUCCESSFUL`
- 현재 코드 기준 핵심 흐름:
  `Admin 판매 준비 -> 공개 조회 -> Hold -> Reservation -> Payment -> Ticket -> Check-in`

흐름 정합성은 아래 기준으로 판단한다.

- 좌석 등록은 `sectionId`가 필수이므로 `Venue -> Section -> Seat -> Event -> Show -> ShowSeat` 순서로 판매 데이터를 준비한다.
- Hold 요청 아이템은 현재 `seatId`만 지원한다.
- Reservation은 본인 소유의 `ACTIVE` hold만 `PENDING_PAYMENT` 예약으로 전환한다.
- Payment는 본인 소유 예약, 예약 총액, `Idempotency-Key`를 검증한 뒤 `PAID` 처리와 티켓 발급을 한 트랜잭션에서 수행한다.
- Ticket은 `reservation_items` 기준으로 좌석 1개당 1장 발급된다.
- Check-in은 `ADMIN` 또는 `MASTER_ADMIN`만 수행할 수 있고, 같은 `qrToken` 재사용은 `409 Conflict`가 되어야 한다.
- 만료된 hold/reservation은 조회, 가용성 조회, 새 hold 생성, 스케줄러 시점에 lazy release 대상이 된다.

## 테스트 범위

주요 범위는 아래와 같다.

- Auth
- Events / Shows
- Hold
- Reservation
- Payment
- Ticket
- Check-in
- Admin
- 공통 동작

## 권장 도구와 실행 방식

일반 기능 검증은 아래 3단계 조합으로 진행하는 것을 권장한다.

### 1. 자동 회귀 확인: `Gradle test`

- 목적:
  이미 구현된 API 흐름이 깨지지 않았는지 빠르게 확인
- 사용 도구:
  `Spring Boot Test`, `Spring Security Test`, `H2`
- 실행 방법:

```powershell
./gradlew.bat test
```

- 확인 포인트:
  인증, 이벤트/회차 조회, 홀드, 예약, 결제, 체크인, 관리자 API, OpenAPI 문서 관련 통합 테스트가 통과하는지 확인한다.

### 2. 수동 API 검증: `Swagger UI` 또는 `Postman`

- 목적:
  실제 요청/응답 형태와 권한 처리, 상태 전이를 눈으로 검증
- 사용 도구:
  `Swagger UI`, `Postman`
- 권장 방식:
  관리자 판매 준비 API로 테스트 데이터를 만든 뒤, 사용자 시나리오를 순서대로 호출한다.
- 확인 포인트:
  응답 필드, 에러 응답, 상태 전이, 토큰 처리, 후속 조회 결과를 함께 확인한다.

### 3. 성능 테스트 전 최종 게이트: 기능 시나리오 체크리스트

- 목적:
  성능 테스트 전에 기능 정합성이 모두 확인되었는지 판단
- 사용 도구:
  이 문서의 시나리오 체크리스트
- 권장 방식:
  아래 시나리오를 위에서부터 순서대로 실행하고, 각 시나리오의 통과 여부와 실패 원인을 기록한다.

즉, 권장 흐름은 `자동 회귀 확인 -> 수동 API 검증 -> 성능 테스트 진입` 순서다.

## 테스트 전 준비

### 환경 준비

- 로컬 PostgreSQL/Redis 또는 Docker Compose 환경 준비
- JWT 비밀키 설정
- 필요 시 `local` 프로필 활성화

### 계정 준비

- 일반 사용자 계정 1개 이상
- 관리자 계정 1개 이상

### 데이터 준비

- 이벤트와 회차
- 공연장 구역 / 좌석 기준정보
- 회차별 판매 인벤토리

> 좌석 등록 요청은 `sectionId`가 필수이므로, 수동 테스트 데이터는 반드시 구역을 먼저 만들고 좌석을 만든다.

## 권장 검증 순서

기능 검증은 아래 순서로 진행하는 것을 권장한다.

1. 인증
2. 관리자 판매 준비: 공연장, 구역, 좌석, 이벤트, 회차, 회차 판매 좌석
3. 공개 조회
4. 홀드
5. 예약
6. 결제
7. 티켓
8. 체크인
9. 예외 / 권한 / 만료 시나리오

이 순서로 가면 선행 데이터가 자연스럽게 준비되고, 도메인 흐름도 끊기지 않는다.

## 공통 검증 항목

각 시나리오에서 공통으로 확인할 항목이다.

- 상태 코드가 의도와 맞는가
- 응답 필드가 누락 없이 내려오는가
- 권한이 없는 경우 인증/인가 오류가 발생하는가
- 잘못된 입력에 대해 적절한 `4xx`가 발생하는가
- 후속 조회 시 상태 변화가 반영되는가

## 핵심 E2E 시나리오

성능 테스트나 프론트엔드 연동 전에 아래 happy path를 한 번에 통과시킨다.

| 순서 | 행위 | API | 기대 결과 |
| --- | --- | --- | --- |
| 1 | 일반 사용자 가입 / 로그인 | `POST /api/auth/signup`, `POST /api/auth/login` | `userId`, `Authorization` 헤더, `accessToken`, `refreshToken` 확보 |
| 2 | 관리자 로그인 | `POST /api/auth/login` | 관리자 Bearer token 확보 |
| 3 | 공연장 생성 | `POST /api/admin/venues/upsert` | `venueId` 확보 |
| 4 | 구역 생성 | `POST /api/admin/venues/{venueId}/sections` | `createdCount`, `sectionId` 조회 가능 |
| 5 | 좌석 생성 | `POST /api/admin/venues/{venueId}/seats` | `sectionId`가 연결된 `seatId` 확보 |
| 6 | 이벤트 / 회차 생성 | `POST /api/admin/events`, `POST /api/admin/shows` | `eventId`, `showId` 확보 |
| 7 | 회차 판매 좌석 생성 | `POST /api/admin/shows/{showId}/show-seats` | `createdCount` 반환 |
| 8 | 가용성 조회 | `GET /api/shows/{showId}/availability` | 판매 좌석이 `available=true`로 보임 |
| 9 | 좌석 홀드 | `POST /api/holds` | `holdId`, `expiresAt`, 좌석 `HELD` |
| 10 | 예약 생성 | `POST /api/reservations` | `reservationId`, `PENDING_PAYMENT`, hold `CONVERTED`, 좌석 `RESERVED` |
| 11 | 결제 승인 | `POST /api/payments/confirm` | `APPROVED`, 예약 `PAID`, 티켓 발급 |
| 12 | 티켓 조회 | `GET /api/me/tickets` | `ISSUED` 티켓과 `qrToken` 확인 |
| 13 | 체크인 | `POST /api/checkin` | 티켓 `USED`, `usedAt` 기록 |

## 기능 시나리오

### FUNC-AUTH-001 회원가입 성공

- 목적:
  신규 USER 계정이 생성되는지 확인
- 사전 조건:
  중복되지 않는 이메일 준비
- 요청:
  `POST /api/auth/signup`
- 기대 결과:
  `200 OK`
  `userId` 반환
  DB에 USER 계정 저장

### FUNC-AUTH-002 중복 이메일 회원가입 실패

- 목적:
  이메일 unique 제약이 동작하는지 확인
- 사전 조건:
  이미 가입된 이메일 존재
- 요청:
  `POST /api/auth/signup`
- 기대 결과:
  `409 Conflict`

### FUNC-AUTH-003 로그인 성공

- 목적:
  Access Token / Refresh Token 발급 확인
- 요청:
  `POST /api/auth/login`
- 기대 결과:
  `200 OK`
  `Authorization` 헤더 반환
  `refresh-token` Cookie 또는 응답 body 반환
  Redis 토큰 세션이 userId 기준으로 추가되며 최신 5개까지만 유지됨

### FUNC-AUTH-004 로그인 실패

- 목적:
  잘못된 비밀번호 처리 확인
- 요청:
  `POST /api/auth/login`
- 기대 결과:
  `401 Unauthorized`

### FUNC-AUTH-005 토큰 재발급 성공

- 목적:
  Refresh Token 회전 처리 확인
- 요청:
  `POST /api/auth/refresh`
- 기대 결과:
  `200 OK`
  새 Access Token 발급
  새 Refresh Token 발급
  이전 Refresh Token 재사용 시 실패

### FUNC-AUTH-006 로그아웃 성공

- 목적:
  Refresh Token 무효화 확인
- 요청:
  `POST /api/auth/logout`
- 기대 결과:
  `204 No Content`
  요청 refresh token에 해당하는 Redis 토큰 세션 삭제 처리

### FUNC-AUTH-007 내 정보 조회

- 목적:
  인증된 사용자 정보 조회 확인
- 요청:
  `GET /api/auth/me`
- 기대 결과:
  `200 OK`
  `userId`, `email`, `role` 반환

### FUNC-ADM-001 공연장 등록 / 수정

- 목적:
  관리자 API로 공연장 upsert 확인
- 요청:
  `POST /api/admin/venues/upsert`
- 기대 결과:
  `200 OK`
  `venueId` 반환

### FUNC-ADM-002 공연장 구역 등록

- 목적:
  공연장 기준 구역 등록 확인
- 요청:
  `POST /api/admin/venues/{venueId}/sections`
- 기대 결과:
  `200 OK`
  `createdCount` 반환

### FUNC-ADM-003 공연장 좌석 등록

- 목적:
  공연장 기준 좌석 등록 확인
- 사전 조건:
  등록할 좌석마다 유효한 `sectionId` 존재
- 요청:
  `POST /api/admin/venues/{venueId}/seats`
- 기대 결과:
  `200 OK`
  `createdCount` 반환

### FUNC-ADM-004 이벤트 생성

- 목적:
  이벤트 생성 확인
- 요청:
  `POST /api/admin/events`
- 기대 결과:
  `200 OK`
  `eventId` 반환

### FUNC-ADM-005 회차 생성

- 목적:
  이벤트 하위 회차 생성 확인
- 요청:
  `POST /api/admin/shows`
- 기대 결과:
  `200 OK`
  `showId` 반환

### FUNC-ADM-006 회차 좌석 인벤토리 생성

- 목적:
  회차별 좌석 판매 정보 생성 확인
- 사전 조건:
  좌석이 구역에 연결되어 있음
- 요청:
  `POST /api/admin/shows/{showId}/show-seats`
- 기대 결과:
  `200 OK`
  `createdCount` 반환

### FUNC-ADM-007 관리자 준비 데이터 검증 실패

- 목적:
  판매 준비 API의 데이터 정합성 검증 확인
- 요청 / 기대 결과:
  구역 없는 좌석 등록 또는 유효하지 않은 `sectionId`는 `400 Bad Request`
  중복 좌석 label, 중복 구역 name, 중복 회차 판매 좌석은 `409 Conflict`
  다른 관리자가 소유한 venue/event로 upsert 또는 show 생성 시 `403 Forbidden`

### FUNC-ADM-008 공연장 목록 조회

- 목적:
  어드민이 본인이 생성한 공연장 목록과 생성자 정보를 확인
- 요청:
  `GET /api/admin/venues`
- 기대 결과:
  `200 OK`
  현재 관리자가 생성한 공연장만 반환
  응답에 `createdByUserId` 포함

### FUNC-ADM-009 이벤트 목록 조회

- 목적:
  어드민이 본인이 생성한 이벤트 목록과 생성자 정보를 확인
- 요청:
  `GET /api/admin/events`
- 기대 결과:
  `200 OK`
  현재 관리자가 생성한 이벤트만 반환
  응답에 `createdByUserId` 포함

### FUNC-ADM-010 회차 목록 조회

- 목적:
  어드민이 본인이 생성한 회차 목록과 생성자 정보를 확인
- 요청:
  `GET /api/admin/shows`
- 기대 결과:
  `200 OK`
  현재 관리자가 생성한 회차만 반환
  응답에 `createdByUserId`, `eventId`, `eventTitle`, `venueId` 포함

### FUNC-MST-001 전체 공연장 목록 조회

- 목적:
  마스터 관리자가 전체 공연장 목록을 확인
- 요청:
  `GET /api/master/venues`
- 기대 결과:
  `MASTER_ADMIN`은 `200 OK`와 전체 공연장 목록 반환
  일반 `ADMIN`은 `403 Forbidden`

### FUNC-MST-002 전체 이벤트 목록 조회

- 목적:
  마스터 관리자가 전체 이벤트 목록을 확인
- 요청:
  `GET /api/master/events`
- 기대 결과:
  `MASTER_ADMIN`은 `200 OK`와 전체 이벤트 목록 반환
  일반 `ADMIN`은 `403 Forbidden`

### FUNC-MST-003 전체 회차 목록 조회

- 목적:
  마스터 관리자가 전체 회차 목록을 확인
- 요청:
  `GET /api/master/shows`
- 기대 결과:
  `MASTER_ADMIN`은 `200 OK`와 전체 회차 목록 반환
  일반 `ADMIN`은 `403 Forbidden`

### FUNC-VIEW-001 이벤트 목록 조회

- 목적:
  공개 이벤트 목록 조회 확인
- 요청:
  `GET /api/events`
- 기대 결과:
  `200 OK`
  이벤트 목록 반환

### FUNC-VIEW-002 이벤트 상세 조회

- 목적:
  이벤트와 회차 정보 조회 확인
- 요청:
  `GET /api/events/{eventId}`
- 기대 결과:
  `200 OK`
  이벤트 정보와 회차 목록 반환

### FUNC-VIEW-003 회차 가용성 조회

- 목적:
  회차 좌석 가용성 조회 확인
- 요청:
  `GET /api/shows/{showId}/availability`
- 기대 결과:
  `200 OK`
  좌석 목록과 좌석별 구역 정보 반환

### FUNC-VIEW-004 공개 조회 실패 케이스

- 목적:
  공개 조회 API의 입력 검증과 not found 처리 확인
- 요청 / 기대 결과:
  `GET /api/events?status=INVALID`는 `400 Bad Request`
  존재하지 않는 `eventId` 상세 조회는 `404 Not Found`
  존재하지 않는 `showId` 가용성 조회는 `404 Not Found`

### FUNC-HOLD-001 홀드 생성 성공

- 목적:
  좌석 선점 확인
- 요청:
  `POST /api/holds`
- 기대 결과:
  `200 OK`
  `holdId`, `expiresAt` 반환
  요청 좌석은 `HELD` 상태로 반영

### FUNC-HOLD-002 홀드 상세 조회

- 목적:
  홀드 상태와 아이템 목록 확인
- 요청:
  `GET /api/holds/{holdId}`
- 기대 결과:
  `200 OK`
  hold 정보와 item 목록 반환

### FUNC-HOLD-003 홀드 취소

- 목적:
  홀드 취소와 자원 해제 확인
- 요청:
  `DELETE /api/holds/{holdId}`
- 기대 결과:
  `204 No Content`
  좌석 자원 해제

### FUNC-HOLD-004 중복 좌석 홀드 실패

- 목적:
  이미 점유된 좌석 중복 홀드 방지 확인
- 요청:
  같은 `seatId`로 다시 `POST /api/holds`
- 기대 결과:
  `409 Conflict`

### FUNC-HOLD-005 잘못된 아이템 요청 실패

- 목적:
  현재 지원하는 좌석 홀드 요청 형식 검증
- 요청 / 기대 결과:
  `items`가 비어 있거나 `showId`, `items[].seatId`가 누락/음수이면 `400 Bad Request`
  존재하지 않는 `seatId`는 `400 Bad Request`
  같은 요청 안의 중복 `seatId`는 `409 Conflict`

### FUNC-HOLD-006 만료 홀드 lazy release 확인

- 목적:
  만료된 hold가 조회 또는 후속 요청 시 정리되는지 확인
- 사전 조건:
  `expiresAt`이 지난 hold 준비
- 기대 결과:
  상태가 `EXPIRED`로 정리되고 자원이 해제된다.

### FUNC-RES-001 예약 생성 성공

- 목적:
  활성 hold가 예약으로 전환되는지 확인
- 요청:
  `POST /api/reservations`
- 기대 결과:
  `200 OK`
  `reservationId` 반환
  hold 상태 `CONVERTED`
  좌석 상태 `RESERVED`

### FUNC-RES-002 예약 상세 조회

- 목적:
  예약 금액, 상태, 아이템 확인
- 요청:
  `GET /api/reservations/{reservationId}`
- 기대 결과:
  `200 OK`
  예약 상세와 item 목록 반환

### FUNC-RES-003 내 예약 목록 조회

- 목적:
  페이지네이션과 상태 필터 확인
- 요청:
  `GET /api/me/reservations`
- 기대 결과:
  `200 OK`
  페이지 정보와 예약 목록 반환

### FUNC-RES-004 만료 예약 lazy release 확인

- 목적:
  만료된 예약이 조회 시점에 정리되는지 확인
- 사전 조건:
  `PENDING_PAYMENT` 상태의 만료 예약 존재
- 기대 결과:
  상태가 `EXPIRED`로 바뀌고 자원이 해제된다.

### FUNC-RES-005 예약 목록 상태 필터 실패

- 목적:
  `GET /api/me/reservations`의 status 필터 검증 확인
- 요청:
  유효하지 않은 `status` 쿼리로 목록 조회
- 기대 결과:
  `400 Bad Request`

### FUNC-PAY-001 결제 승인 성공

- 목적:
  예약이 `PAID`로 전환되고 티켓이 발급되는지 확인
- 요청:
  `POST /api/payments/confirm`
- 기대 결과:
  `200 OK`
  payment 생성
  reservation 상태 `PAID`
  ticket 생성

### FUNC-PAY-002 결제 멱등 재호출 성공

- 목적:
  같은 `Idempotency-Key` 재호출 시 같은 결과 반환 확인
- 요청:
  같은 key와 같은 body로 재호출
- 기대 결과:
  같은 payment 응답
  티켓 중복 발급 없음

### FUNC-PAY-003 금액 불일치 실패

- 목적:
  예약 총액과 다른 결제 요청 차단 확인
- 요청:
  잘못된 amount로 `POST /api/payments/confirm`
- 기대 결과:
  `409 Conflict`

### FUNC-PAY-004 멱등키 재사용 충돌

- 목적:
  같은 `Idempotency-Key`를 다른 예약 또는 다른 금액 요청에 재사용하지 못하는지 확인
- 요청:
  이미 승인된 key와 다른 body로 `POST /api/payments/confirm`
- 기대 결과:
  `409 Conflict`

### FUNC-PAY-005 결제 헤더 검증 실패

- 목적:
  결제 승인 API가 `Idempotency-Key` 헤더를 필수로 요구하는지 확인
- 요청:
  `Idempotency-Key` 없이 `POST /api/payments/confirm`
- 기대 결과:
  `400 Bad Request`

### FUNC-TKT-001 내 티켓 목록 조회

- 목적:
  결제 이후 발급된 티켓 조회 확인
- 요청:
  `GET /api/me/tickets`
- 기대 결과:
  `200 OK`
  ticket 목록 반환

### FUNC-CHK-001 체크인 성공

- 목적:
  QR 토큰 기반 티켓 사용 처리 확인
- 요청:
  `POST /api/checkin`
- 기대 결과:
  `200 OK`
  ticket 상태 `USED`
  `usedAt` 기록

### FUNC-CHK-002 중복 체크인 실패

- 목적:
  이미 사용된 티켓 재입장 방지 확인
- 요청:
  동일한 `qrToken`으로 재호출
- 기대 결과:
  `409 Conflict`

### FUNC-CHK-003 유효하지 않은 QR 토큰 실패

- 목적:
  존재하지 않는 `qrToken` 요청 처리 확인
- 요청:
  잘못된 `qrToken`으로 `POST /api/checkin`
- 기대 결과:
  `404 Not Found`

### FUNC-CHK-004 일반 사용자 체크인 실패

- 목적:
  체크인 API의 관리자 권한 제한 확인
- 요청:
  USER 토큰으로 `POST /api/checkin`
- 기대 결과:
  `403 Forbidden`

## 권한 검증 시나리오

### FUNC-SEC-001 비인증 사용자 접근 차단

- 대상:
  `/api/holds`, `/api/reservations`, `/api/payments/confirm`, `/api/me/*`
- 기대 결과:
  `401 Unauthorized`

### FUNC-SEC-002 일반 사용자의 관리자 API 접근 차단

- 대상:
  `/api/admin/**`, `POST /api/checkin`
- 기대 결과:
  `403 Forbidden`

### FUNC-SEC-003 관리자 접근 허용

- 대상:
  관리자 API
- 기대 결과:
  정상 처리

## 회귀 테스트 묶음

릴리즈 전 또는 큰 변경 후에는 아래 묶음을 최소 회귀 세트로 권장한다.

### 회귀 세트 A: 인증

- 회원가입
- 로그인
- 토큰 재발급
- 로그아웃
- 내 정보 조회

### 회귀 세트 B: 예매 핵심 흐름

- 회차 가용성 조회
- 홀드 생성
- 예약 생성
- 결제 승인
- 티켓 조회
- 체크인

### 회귀 세트 C: 관리자 준비 흐름

- 공연장 생성
- 구역 / 좌석 등록
- 이벤트 생성
- 회차 생성
- 회차 인벤토리 생성

## 테스트 결과 기록 형식

기능 테스트 결과는 아래 형식으로 기록하는 것을 권장한다.

```text
시나리오 ID:
테스트 일시:
환경:
사전 조건:
입력:
기대 결과:
실제 결과:
판정:
비고:
```

## 다음 단계 제안

- 기존 통합 테스트와 이 문서의 시나리오 ID를 맞춘다.
- 정적 테스트 페이지와 기능 시나리오 문서를 연결한다.
- 이후 성능 테스트는 이 문서의 주요 시나리오가 모두 통과했다는 전제에서 시작한다.
