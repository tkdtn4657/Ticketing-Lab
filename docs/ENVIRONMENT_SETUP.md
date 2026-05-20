# 환경 설정

## 프로필 규칙
- `src/main/resources/application.yml`은 모든 실행 환경이 공통으로 사용하는 기본 설정이다.
- `src/main/resources/application-local.yml`은 `local` 프로필이 활성화될 때만 적용된다.
- `src/main/resources/application-docker.yml`은 `docker` 프로필이 활성화될 때만 적용된다.
- `src/main/resources/application-local-secret.yml`은 로컬 비밀값을 두는 Git 미추적 파일이다.
- `src/test/resources/application.yml`은 자동화 테스트 전용 설정이며, 샘플 데이터를 비활성화한다.

## 로컬 실행 전 준비
- `src/main/resources/application-local-secret.example.yml`을 참고해 `src/main/resources/application-local-secret.yml`을 만든다.
- `jwt.secret`에는 Base64로 인코딩된 32바이트 이상 비밀키를 넣는다.
- DB 접속 정보는 기본값을 그대로 쓰거나 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 환경변수로 덮어쓴다.
- Redis 접속 정보는 기본값 `localhost:6379`를 사용하거나 `REDIS_HOST`, `REDIS_PORT` 환경변수로 덮어쓴다.
- `JWT_SECRET` 환경변수를 써도 되지만, 로컬에서는 `application-local-secret.yml`로 관리하는 편이 실수하기 덜 쉽다.

## 이벤트 샘플 데이터
- `EventSampleDataInitializer`는 `local` 프로필에서만 실행된다.
- 초기화기는 `app.sample-data.events.enabled=true`일 때만 동작한다.
- 이미 이벤트가 하나라도 있으면 샘플 데이터를 다시 넣지 않는다.

## 시간대와 파일 로그
- 애플리케이션 기본 시간대는 `Asia/Seoul`이다.
- Docker 이미지는 `TZ=Asia/Seoul`, `JAVA_OPTS=-Duser.timezone=Asia/Seoul`로 실행한다.
- 파일 로그는 `LOG_PATH` 아래에 날짜별 `yyyyMMdd_back.log` 형식으로 저장한다.
- Docker Compose 실행 시 기본 로그 경로는 백엔드 레포 기준 `./logs`이며, 통합 워크스페이스 Compose에서는 `./backend/logs`가 컨테이너의 `/app/logs`에 마운트된다.
- 컨테이너는 시작 시 `/app/logs`를 생성하고 `app` 사용자 소유로 정리한 뒤 애플리케이션을 실행한다.

## 로컬 관리자 계정
- `AdminAccountInitializer`는 `local` 프로필에서만 실행된다.
- 초기화기는 `app.sample-data.admin.enabled=true`일 때만 동작한다.
- 기본 로컬 관리자 계정은 `admin@example.com` / `admin1234` 이다.
- 필요하면 `APP_SAMPLE_ADMIN_EMAIL`, `APP_SAMPLE_ADMIN_PASSWORD` 환경변수로 덮어쓴다.
- 동일 이메일 사용자가 이미 있으면 다시 생성하지 않는다.

## 이렇게 분리한 이유
- 공통 설정 파일에는 비밀값을 두지 않아 공개 저장소에 올려도 위험도를 낮춘다.
- 샘플 데이터는 로컬 UI/API 확인에는 유용하다.
- 테스트는 미리 적재된 데이터에 의존하지 않아야 한다.
- 로컬이 아닌 환경에서 데모 데이터가 실수로 생성되면 안 된다.

## 샘플 데이터와 함께 실행하는 방법
- IntelliJ: Spring Boot 실행 구성의 활성 프로파일에 `local`을 입력한다.
- CLI: `./gradlew bootRun --args='--spring.profiles.active=local'`로 실행한다.

## Docker Compose로 백엔드만 실행하는 방법
- 백엔드 루트에는 PostgreSQL, Redis, 애플리케이션을 함께 올리는 `docker-compose.yml`이 있다.
- 먼저 `.env.example`을 복사해서 `.env`를 만든다.
- 기본값만으로도 로컬 개발용 실행이 가능하지만, `JWT_SECRET`은 필요 시 원하는 값으로 바꿔도 된다.

```powershell
Copy-Item .env.example .env
docker compose up --build
```

- 기본 포트는 `9090`(backend), `5432`(postgres), `6379`(redis) 이다.
- Docker Compose 실행 시 애플리케이션은 `docker` 프로필로 구동된다.
- `docker` 프로필에서도 샘플 이벤트 데이터와 로컬 관리자 계정을 사용할 수 있다.
- 관리자 계정 기본값은 `admin@example.com` / `admin1234` 이다.

## 인증 토큰 세션 저장소
- 기본 토큰 세션 저장소는 Redis이며 `app.auth.token-session.store=redis`로 동작한다.
- 자동화 테스트에서는 외부 Redis에 의존하지 않도록 `app.auth.token-session.store=in-memory`를 사용한다.
- Redis 연결 타임아웃은 기본 3초, 명령 실행 타임아웃은 기본 2초이며 `REDIS_CONNECT_TIMEOUT`, `REDIS_TIMEOUT`으로 조정할 수 있다.
- 로그인 성공 시 Redis에는 `auth:user:sessions:{userId}` Sorted Set에 refresh token id가 추가되고, 세션 본문은 `auth:session:{userId}:{refreshTokenId}`로 저장된다.
- 사용자별 토큰 세션은 기본 5개까지 유지하며, 한도를 넘으면 가장 오래된 세션과 해당 access token 인덱스를 함께 제거한다.
- refresh 성공 시 기존 refresh token 세션은 새 access/refresh token 세션으로 원자적으로 교체된다.
- Access Token 만료 시간은 5시간, Refresh Token 만료 시간과 Redis 세션 TTL은 14일이다.

## Hold 좌석 선점 보호 설정

동일 좌석에 요청이 몰릴 때는 좌석별 queue와 Redis pre-lock을 함께 사용한다.

기본값은 아래와 같다.

| 환경변수 | 기본값 | 설명 |
| --- | --- | --- |
| `HOLD_SEAT_QUEUE_ENABLED` | `true` | 좌석별 요청 queue 사용 여부 |
| `HOLD_SEAT_QUEUE_MAX_PER_SEAT` | `100` | 좌석 하나에 동시에 진입 가능한 요청 슬롯 수 |
| `HOLD_SEAT_QUEUE_TTL` | `30s` | queue 슬롯이 비정상 종료 시 자동 해제되는 시간 |
| `HOLD_PRE_LOCK_ENABLED` | `true` | Redis pre-lock 사용 여부 |
| `HOLD_PRE_LOCK_TTL` | `60s` | pre-lock TTL |
| `HOLD_FAST_FAIL_ENABLED` | `false` | 실험용 Spring fast-fail bulkhead 사용 여부 |
| `HOLD_FAST_FAIL_MAX_CONCURRENT` | `50` | fast-fail 사용 시 Hold 생성 동시 처리 슬롯 수 |

채택한 기본 정책은 `HOLD_SEAT_QUEUE_ENABLED=true`와 `HOLD_PRE_LOCK_ENABLED=true` 조합이다. `HOLD_FAST_FAIL_ENABLED`는 이전 실험용 보조 보호선이므로 기본값을 `false`로 둔다.

테스트 프로필에서는 외부 Redis 의존성을 줄이기 위해 pre-lock과 queue를 비활성화하고, 필요한 단위 테스트에서는 mock으로 동작을 검증한다.

## Swagger / OpenAPI 확인 경로
- Swagger UI: `/docs/swagger-ui.html`
- OpenAPI JSON: `/docs/api-docs`
- OpenAPI YAML: `/docs/api-docs.yaml`
- Swagger는 `/api/**`만 노출하며 `Bearer` 인증을 기본으로 사용한다.
- 공개 API를 제외한 호출은 Swagger UI 우측 상단 `Authorize`에서 `Bearer {accessToken}` 형식으로 입력한다.
- Swagger 예시는 통합 테스트와 정적 테스트 페이지의 요청/응답 흐름을 기준으로 관리한다.

## 함께 확인하기 좋은 테스트 페이지
- `/auth-test.html`
- `/events-test.html`
- `/shows-test.html`
- `/holds-test.html`
- `/reservations-test.html`
- `/payments-test.html`
- `/checkin-test.html`
- `/admin-test.html`

## 현재 로컬 동작 방식
- `local` 프로필에서는 이벤트 샘플 데이터를 활성화한다.
- `local` 프로필에서는 `spring.jpa.hibernate.ddl-auto=update`를 사용해 재시작 때마다 스키마를 다시 만들지 않도록 한다.
- `local` 프로필에서는 DB 기본값을 로컬 개발용으로 제공하고, JWT 비밀키는 별도 파일이나 환경변수로만 받는다.
- `local` 프로필에서는 Redis 기본값을 `localhost:6379`로 사용한다.
