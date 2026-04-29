# Ticketing Lab Backend

`Ticketing Lab Backend`는 공연/이벤트 티켓 예매 시스템 MVP를 위한 Spring Boot 백엔드입니다.
단순 CRUD 서버가 아니라, 실제 예매 도메인에서 자주 마주치는 상태 전이와 재고 관리 문제를 백엔드 중심으로 다루는 것을 목표로 합니다.

프론트엔드와 안드로이드 클라이언트는 이 백엔드를 기준으로 API를 연동하며, 도메인 용어와 요청/응답 스펙도 이 저장소를 기준으로 맞춥니다.

## 핵심 요약

- 핵심 흐름: `Hold -> Reservation -> Payment -> Ticket -> Check-in`
- 판매 단위: `Show(회차)`
- 인벤토리 모델: `지정형 Seat` 우선, `Section 수량형` 확장 지원
- 아키텍처: 모놀리식 `Spring MVC`
- 목적: 예매 도메인의 상태 전이, 재고 보호, 결제 멱등성, 체크인 흐름을 일관된 모델로 검증

## 프로젝트 목적

이 프로젝트는 공연 예매 도메인을 통해 아래 같은 실전형 주제를 다루기 위해 만들었습니다.

- 좌석/구역 선점과 재고 보호
- Hold / Reservation 만료 처리
- `Idempotency-Key` 기반 결제 멱등성
- 결제 이후 티켓 발급과 QR 기반 체크인
- 웹과 모바일이 함께 붙을 수 있는 API 중심 구조

즉, 클라이언트는 바뀌더라도 도메인 규칙은 백엔드에서 안정적으로 유지되는 구조를 지향합니다.

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

## 현재 구현 범위

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

상세 API 목록과 구현 상태는 [docs/API_CATALOG.md](docs/API_CATALOG.md)에서 확인할 수 있습니다.

## 핵심 도메인 흐름

```text
Show -> Hold -> Reservation -> Payment -> Ticket -> Check-in
```

- Hold: `ACTIVE -> CONVERTED | CANCELED | EXPIRED`
- Reservation: `PENDING_PAYMENT -> PAID | CANCELED | EXPIRED`
- Payment: `REQUESTED -> APPROVED | FAILED`
- Ticket: `ISSUED -> USED | CANCELED(optional)`

## 아키텍처 방향

이 프로젝트는 모놀리식 Spring MVC 애플리케이션이지만, 내부 구조는 기능 기준으로 분리합니다.

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

설계 방향은 다음과 같습니다.

- Controller는 thin 하게 유지합니다.
- 유스케이스는 작업 단위가 분명하도록 분리합니다.
- 도메인 규칙은 서비스나 컨트롤러에 흩어지지 않도록 도메인 객체 중심으로 모읍니다.
- 프론트엔드와 안드로이드는 백엔드 API와 도메인 용어를 기준으로 맞춥니다.

## 기술 스택

- Java 17
- Spring Boot 3.5.x
- Spring MVC
- Spring Security
- Spring Data JPA
- Spring Data Redis
- PostgreSQL
- Redis
- springdoc-openapi
- JWT
- Gradle
- H2 테스트 데이터베이스

## 빠른 실행

### 1. 실행 환경 준비

- Java 17
- PostgreSQL
- Redis

기본 로컬 설정:

- DB URL: `jdbc:postgresql://localhost:5432/ticketing`
- DB Username: `postgres`
- DB Password: `postgres`
- Redis: `localhost:6379`
- 서버 포트: `9090`

필요하면 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT` 환경변수로 덮어쓸 수 있습니다.

### 2. 로컬 비밀값 파일 만들기

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

기본 실행 프로필은 `local`입니다.

```powershell
./gradlew.bat bootRun
```

명시적으로 실행하려면:

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

기본 로컬 관리자 계정:

- 이메일: `admin@example.com`
- 비밀번호: `admin1234`

정적 테스트 페이지:

- `/auth-test.html`
- `/events-test.html`
- `/shows-test.html`
- `/holds-test.html`
- `/reservations-test.html`
- `/payments-test.html`
- `/checkin-test.html`
- `/admin-test.html`

## 운영과 확장 관점

이 프로젝트는 소개형 README를 유지하기 위해 운영 메모의 상세 내용은 `docs/`로 분리했습니다.
다만 백엔드의 방향성을 보여주는 핵심 포인트는 아래와 같습니다.

- 성능: 공개 조회 API, 홀드 생성, 예약 전환, 결제 멱등성을 중심으로 부하 시나리오를 설계하고 있습니다.
- 고가용성: JWT 기반 stateless 확장성, 다중 인스턴스 배치, DB 병목 완화 전략을 다음 단계로 검토합니다.
- 로그/관측성: `traceId`, 공통 에러 응답, 구조화 로그, 운영 추적 체계를 후속 과제로 관리합니다.

이 영역의 상세 계획은 [docs/PERFORMANCE_TEST_PLAN.md](docs/PERFORMANCE_TEST_PLAN.md), [docs/REQUIREMENTS_BACKLOG.md](docs/REQUIREMENTS_BACKLOG.md)에서 확인할 수 있습니다.

## 문서 바로가기

- [docs/PROJECT_OVERVIEW.md](docs/PROJECT_OVERVIEW.md): 목표, 도메인 모델, 상태 전이, MVP 범위
- [docs/API_CATALOG.md](docs/API_CATALOG.md): API 목록, 요청/응답 개요, 구현 상태
- [docs/REQUIREMENTS_BACKLOG.md](docs/REQUIREMENTS_BACKLOG.md): 요구사항 백로그와 완료 조건
- [docs/ENVIRONMENT_SETUP.md](docs/ENVIRONMENT_SETUP.md): 실행 환경과 프로필 설명
- [docs/FUNCTIONAL_TEST_SCENARIOS.md](docs/FUNCTIONAL_TEST_SCENARIOS.md): 일반 기능 검증 시나리오
- [docs/PERFORMANCE_TEST_PLAN.md](docs/PERFORMANCE_TEST_PLAN.md): 성능/트래픽 테스트 도구, 절차, 시나리오

## 테스트 실행

통합 테스트를 포함한 전체 테스트는 다음 명령으로 실행할 수 있습니다.

```powershell
./gradlew.bat test
```

일반 기능 검증 시나리오는 [docs/FUNCTIONAL_TEST_SCENARIOS.md](docs/FUNCTIONAL_TEST_SCENARIOS.md), 성능/트래픽 테스트 계획은 [docs/PERFORMANCE_TEST_PLAN.md](docs/PERFORMANCE_TEST_PLAN.md)에 정리했습니다.

## Docker Compose 실행

### 1. 백엔드 단독 실행

이 레포지토리 안에는 PostgreSQL, Redis, 백엔드를 함께 올리는 전용 `docker-compose.yml`이 포함되어 있습니다.
이 구성은 프론트엔드와 nginx를 제외하고, `PostgreSQL + Redis + Backend` 조합만 빠르게 확인하거나 백엔드 기능 검증을 진행할 때 사용합니다.

실행 순서는 아래와 같습니다.

```powershell
Copy-Item .env.example .env
docker compose up --build
```

기존 `postgres:16-alpine` 볼륨이 남아 있다면 PostgreSQL 18 컨테이너가 같은 데이터 디렉터리를 그대로 열 수 없습니다.
로컬 개발 데이터가 필요 없으면 `docker compose down -v`로 볼륨을 지운 뒤 다시 올리고, 데이터 보존이 필요하면 `pg_upgrade` 또는 dump/restore 절차를 사용합니다.

필요하면 백그라운드 실행도 가능합니다.

```powershell
docker compose up -d --build
```

이 `docker compose`는 내부적으로 다음 구성을 사용합니다.

- `postgres:18-alpine`
- `redis:8.4.2-alpine`
- `backend` 애플리케이션 컨테이너
- `docker` 프로필 기반 Spring Boot 실행
- `actuator/health` 기반 헬스체크
- 개발용 샘플 이벤트 데이터 / 관리자 계정 초기화

실행 후 기본 접속 경로:

- API 서버: `http://localhost:9090`
- Swagger UI: `http://localhost:9090/docs/swagger-ui.html`
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`

기본 개발용 샘플 데이터와 관리자 계정도 함께 사용할 수 있습니다.

- 관리자 계정: `admin@example.com`
- 관리자 비밀번호: `admin1234`

`.env.example` 기준으로 자주 조정하는 값은 아래와 같습니다.

- `BACKEND_PORT`: 백엔드 외부 포트
- `POSTGRES_PORT`: PostgreSQL 외부 포트
- `REDIS_PORT`: Redis 외부 포트
- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`: DB 접속 정보
- `JWT_SECRET`: JWT 서명 키
- `APP_SAMPLE_EVENTS_ENABLED`: 샘플 이벤트 데이터 생성 여부
- `APP_SAMPLE_ADMIN_ENABLED`: 샘플 관리자 계정 생성 여부

종료나 볼륨 정리는 아래 명령을 사용하면 됩니다.

```powershell
docker compose down
docker compose down -v
```

환경변수 설명은 [docs/ENVIRONMENT_SETUP.md](docs/ENVIRONMENT_SETUP.md)를 참고하세요.

### 2. 풀스택 통합 실행

프론트엔드, 백엔드, PostgreSQL을 함께 올리는 통합 `Docker Compose` 실행 환경은 별도의 워크스페이스 루트에서 관리합니다.
이 워크스페이스 루트는 통합 개발용 저장소이며, 현재 공개된 이 백엔드 레포지토리에서는 보이지 않을 수 있습니다.

## 한 줄 정리

이 백엔드는 공연 예매 도메인의 핵심 흐름을 API 중심으로 구현하고, 웹과 모바일이 함께 붙을 수 있는 기준 서버를 만드는 것을 목표로 하는 프로젝트입니다.
