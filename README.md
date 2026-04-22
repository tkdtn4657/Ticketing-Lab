# Ticketing Lab Backend

`Ticketing Lab Backend`는 공연/이벤트 티켓 예매 시스템 MVP를 위한 Spring Boot 백엔드입니다.
이 프로젝트는 단순한 CRUD 서버가 아니라, 실제 예매 도메인에서 자주 마주치는 상태 전이와 재고 관리 흐름을 백엔드 중심으로 다루기 위해 만들어졌습니다.

프론트엔드와 안드로이드 클라이언트는 이 백엔드를 기준으로 API를 연동하며, 도메인 용어와 요청/응답 스펙도 이 저장소를 기준으로 맞춥니다.

## 프로젝트 생성 의도

- 공연 예매 도메인을 통해 `Hold -> Reservation -> Payment -> Ticket -> Check-in` 흐름을 일관된 모델로 구현하기 위해 만들었습니다.
- 좌석 선점, 예약 만료, 결제 멱등성, 체크인 같은 실전형 주제를 학습하고 검증할 수 있는 백엔드 예제가 필요했습니다.
- 웹과 모바일이 함께 붙을 수 있는 API 중심 구조를 만들고, 클라이언트가 바뀌어도 도메인 규칙은 백엔드에서 안정적으로 유지되도록 설계하고 싶었습니다.
- 기능이 늘어나더라도 읽기 쉽고 유지보수하기 쉬운 구조를 유지하기 위해 기능 기준 패키지 분리와 계층 분리를 실험하는 목적도 있습니다.

## 이 프로젝트를 어디에 활용할 수 있나

- 티켓 예매 서비스 MVP 또는 사내 프로토타입의 출발점으로 활용할 수 있습니다.
- Spring MVC, Spring Security, JPA 기반의 실전형 백엔드 포트폴리오 예제로 활용할 수 있습니다.
- 프론트엔드, 안드로이드와 협업할 때 API 계약의 기준 서버로 활용할 수 있습니다.
- 예매 시스템에서 중요한 동시성, 만료 처리, 멱등 결제, 체크인 흐름을 학습하는 교육용 프로젝트로 활용할 수 있습니다.
- 관리자용 판매 준비 API와 사용자 예매 API를 한 저장소에서 함께 다루는 도메인 설계 연습용으로 활용할 수 있습니다.

## 핵심 도메인 흐름

이 프로젝트의 핵심 흐름은 아래와 같습니다.

```text
Show -> Hold -> Reservation -> Payment -> Ticket -> Check-in
```

- 판매 단위는 `Show(회차)` 입니다.
- 인벤토리는 `지정형 Seat`를 우선 지원합니다.
- 확장 모델로 `Section 수량형`도 함께 다룹니다.

주요 상태 전이는 다음과 같습니다.

- Hold: `ACTIVE -> CONVERTED | CANCELED | EXPIRED`
- Reservation: `PENDING_PAYMENT -> PAID | CANCELED | EXPIRED`
- Payment: `REQUESTED -> APPROVED | FAILED`
- Ticket: `ISSUED -> USED | CANCELED(optional)`

## 핵심 사용자 시나리오

### 1. 관리자 판매 준비

- 공연장을 등록하거나 수정합니다.
- 공연장 기준 좌석과 구역 정보를 등록합니다.
- 이벤트와 회차를 생성합니다.
- 회차별 판매 좌석과 구역 재고를 생성합니다.

### 2. 사용자 예매

- 이벤트 목록과 상세, 회차 가용성을 조회합니다.
- 좌석 또는 구역 수량을 홀드합니다.
- 홀드를 예약으로 전환합니다.
- 결제를 승인하면 티켓이 발급됩니다.

### 3. 현장 체크인

- 관리자 권한으로 QR 토큰 기반 체크인을 수행합니다.
- 이미 사용된 티켓은 중복 입장을 막기 위해 다시 체크인할 수 없습니다.

## 현재 구현된 주요 기능

| 영역 | 주요 내용 |
| --- | --- |
| Auth | 회원가입, 로그인, 토큰 재발급, 로그아웃, 내 정보 조회 |
| Events / Shows | 이벤트 목록 조회, 이벤트 상세 조회, 회차 가용성 조회 |
| Holds | 좌석/구역 홀드 생성, 상세 조회, 취소 |
| Reservations | 홀드 기반 예약 생성, 예약 상세 조회, 내 예약 목록 조회 |
| Payments | `Idempotency-Key` 기반 결제 승인, 예약 상태 전이 |
| Tickets | 결제 성공 후 티켓 발급, 내 티켓 목록 조회 |
| Check-in | QR 토큰 기반 입장 처리, 중복 체크인 방지 |
| Admin | 공연장/좌석/구역 등록, 이벤트/회차 생성, 회차 인벤토리 생성 |

자세한 API 목록과 상태는 [docs/API_CATALOG.md](docs/API_CATALOG.md)에서 확인할 수 있습니다.

## 기술 스택

- Java 17
- Spring Boot 3.5.x
- Spring MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- springdoc-openapi
- JWT
- Gradle
- H2 테스트 데이터베이스

## 아키텍처 방향

이 프로젝트는 모놀리식 Spring MVC 애플리케이션이지만, 내부 구조는 기능 중심으로 분리합니다.

```text
src/main/java/com/ticketinglab/
  auth/
  admin/
  event/
  show/
  hold/
  reservation/
  payment/
  ticket/
  checkin/
  venue/
  user/
```

각 기능은 기본적으로 다음 계층을 기준으로 구성합니다.

- `presentation`: Controller, Request/Response DTO
- `application`: UseCase, 오케스트레이션
- `domain`: 엔티티, 상태, 규칙, 저장소 포트
- `infrastructure`: JPA 구현체, 외부 연동 구현

설계 원칙은 다음과 같습니다.

- Controller는 가능한 한 thin 하게 유지합니다.
- 유스케이스는 작업 단위가 분명하도록 분리합니다.
- 도메인 규칙은 서비스나 컨트롤러에 흩어지지 않도록 도메인 객체 중심으로 모읍니다.
- 프론트엔드와 안드로이드는 백엔드 API와 도메인 용어를 기준으로 맞춥니다.

## 문서 바로가기

- [docs/PROJECT_OVERVIEW.md](docs/PROJECT_OVERVIEW.md): 목표, 도메인 모델, 상태 전이, MVP 범위
- [docs/API_CATALOG.md](docs/API_CATALOG.md): API 목록, 요청/응답 개요, 구현 상태
- [docs/REQUIREMENTS_BACKLOG.md](docs/REQUIREMENTS_BACKLOG.md): 요구사항 백로그와 완료 조건
- [docs/ENVIRONMENT_SETUP.md](docs/ENVIRONMENT_SETUP.md): 실행 환경과 프로필 설명

## 로컬 실행 방법

### 1. 실행 환경 준비

다음 환경을 먼저 준비합니다.

- Java 17
- PostgreSQL

기본 로컬 설정은 아래 값을 사용합니다.

- DB URL: `jdbc:postgresql://localhost:5432/ticketing`
- DB Username: `postgres`
- DB Password: `postgres`
- 서버 포트: `9090`

필요하면 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 환경변수로 덮어쓸 수 있습니다.

### 2. 로컬 비밀값 파일 만들기

예시 파일을 복사해 로컬 비밀값 파일을 만듭니다.

```powershell
Copy-Item src/main/resources/application-local-secret.example.yml src/main/resources/application-local-secret.yml
```

`application-local-secret.yml`에는 Base64 인코딩된 JWT 비밀키를 넣어야 합니다.

예시:

```yaml
jwt:
  secret: "Base64-encoded-32bytes-or-more-secret"
```

### 3. 애플리케이션 실행

이 프로젝트는 별도 프로필을 주지 않으면 기본적으로 `local` 프로필로 실행되도록 되어 있습니다.

```powershell
./gradlew.bat bootRun
```

명시적으로 실행하려면 다음 명령을 사용할 수 있습니다.

```powershell
./gradlew.bat bootRun --args='--spring.profiles.active=local'
```

### 4. 접속 경로 확인

- API 서버: `http://localhost:9090`
- Swagger UI: `http://localhost:9090/docs/swagger-ui.html`
- OpenAPI JSON: `http://localhost:9090/docs/api-docs`
- OpenAPI YAML: `http://localhost:9090/docs/api-docs.yaml`

## 로컬 개발 편의 기능

`local` 프로필에서는 개발 편의를 위해 다음 기능이 활성화됩니다.

- 이벤트 샘플 데이터 초기화
- 로컬 관리자 계정 생성
- `spring.jpa.hibernate.ddl-auto=update`

기본 로컬 관리자 계정은 다음과 같습니다.

- 이메일: `admin@example.com`
- 비밀번호: `admin1234`

샘플 계정과 샘플 데이터는 로컬 개발용입니다. 운영 환경에서는 반드시 비활성화하는 것을 권장합니다.

## 테스트 페이지

정적 테스트 페이지로 주요 흐름을 빠르게 확인할 수 있습니다.

- `/auth-test.html`
- `/events-test.html`
- `/shows-test.html`
- `/holds-test.html`
- `/reservations-test.html`
- `/payments-test.html`
- `/checkin-test.html`
- `/admin-test.html`

## 테스트 실행

통합 테스트를 포함한 전체 테스트는 다음 명령으로 실행할 수 있습니다.

```powershell
./gradlew.bat test
```

현재 테스트는 인증, 이벤트 조회, 홀드, 예약, 결제, 체크인, 관리자 API, OpenAPI 문서 노출을 중심으로 구성되어 있습니다.

## Docker Compose로 함께 실행하기

백엔드만 따로 실행할 수도 있지만, 워크스페이스 루트에서는 PostgreSQL, 백엔드, 프론트엔드를 함께 올릴 수 있습니다.

워크스페이스 루트에서:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

이 방식은 프론트엔드까지 포함해 전체 흐름을 확인할 때 유용합니다.
자세한 내용은 워크스페이스 루트의 [워크스페이스 README](../README.md)를 참고하세요.

## 현재 한계와 확장 방향

이 프로젝트는 MVP를 목표로 하므로, 아래 항목은 앞으로 확장하기 좋은 주제입니다.

- 외부 결제 게이트웨이 연동을 포함한 실제 결제사 연동
- OAuth2 로그인
- 운영 환경용 DB 마이그레이션 도구 도입
- 좌석 선점/예약 구간의 동시성 전략 고도화
- 모니터링, 감사 로그, 알림 등 운영 기능 강화
- 프론트엔드/안드로이드와의 계약 테스트 자동화

## 한 줄 정리

이 백엔드는 공연 예매 도메인의 핵심 흐름을 API 중심으로 구현하고, 웹과 모바일이 함께 붙을 수 있는 기준 서버를 만드는 것을 목표로 하는 프로젝트입니다.
