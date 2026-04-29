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
- 공연장 좌석 / 구역 기준정보
- 회차별 판매 인벤토리

## 권장 검증 순서

기능 검증은 아래 순서로 진행하는 것을 권장한다.

1. 인증
2. 관리자 판매 준비
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
  Redis 단일 세션이 userId 기준으로 갱신됨

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
  Redis 토큰 세션 삭제 처리

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

### FUNC-ADM-002 공연장 좌석 등록

- 목적:
  공연장 기준 좌석 등록 확인
- 요청:
  `POST /api/admin/venues/{venueId}/seats`
- 기대 결과:
  `200 OK`
  `createdCount` 반환

### FUNC-ADM-003 공연장 구역 등록

- 목적:
  공연장 기준 구역 등록 확인
- 요청:
  `POST /api/admin/venues/{venueId}/sections`
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
- 요청:
  `POST /api/admin/shows/{showId}/show-seats`
- 기대 결과:
  `200 OK`
  `createdCount` 반환

### FUNC-ADM-007 회차 구역 인벤토리 생성

- 목적:
  회차별 구역 재고 생성 확인
- 요청:
  `POST /api/admin/shows/{showId}/section-inventories`
- 기대 결과:
  `200 OK`
  `createdCount` 반환

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
  좌석 / 구역 가용성 조회 확인
- 요청:
  `GET /api/shows/{showId}/availability`
- 기대 결과:
  `200 OK`
  좌석과 구역 정보 반환

### FUNC-HOLD-001 홀드 생성 성공

- 목적:
  좌석 / 구역 선점 확인
- 요청:
  `POST /api/holds`
- 기대 결과:
  `200 OK`
  `holdId`, `expiresAt` 반환
  좌석은 `HELD`, 구역은 `hold_qty` 증가

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
  좌석 / 구역 자원 해제

### FUNC-HOLD-004 중복 좌석 홀드 실패

- 목적:
  이미 점유된 좌석 중복 홀드 방지 확인
- 요청:
  같은 `seatId`로 다시 `POST /api/holds`
- 기대 결과:
  `409 Conflict`

### FUNC-HOLD-005 잘못된 아이템 요청 실패

- 목적:
  `seatId`와 `sectionId`를 동시에 보내거나 둘 다 비운 요청 검증
- 기대 결과:
  `400 Bad Request`

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
- 좌석 / 구역 등록
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
