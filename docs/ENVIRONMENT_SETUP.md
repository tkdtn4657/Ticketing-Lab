# 환경 설정

## 프로필 규칙
- `src/main/resources/application.yml`은 모든 실행 환경이 공통으로 사용하는 기본 설정이다.
- `src/main/resources/application-local.yml`은 `local` 프로필이 활성화될 때만 적용된다.
- `src/test/resources/application.yml`은 자동화 테스트 전용 설정이며, 샘플 데이터를 비활성화한다.

## 이벤트 샘플 데이터
- `EventSampleDataInitializer`는 `local` 프로필에서만 실행된다.
- 초기화기는 `app.sample-data.events.enabled=true`일 때만 동작한다.
- 이미 이벤트가 하나라도 있으면 샘플 데이터를 다시 넣지 않는다.

## 이렇게 분리한 이유
- 샘플 데이터는 로컬 UI/API 확인에는 유용하다.
- 테스트는 미리 적재된 데이터에 의존하지 않아야 한다.
- 로컬이 아닌 환경에서 데모 데이터가 실수로 생성되면 안 된다.

## 샘플 데이터와 함께 실행하는 방법
- IntelliJ: Spring Boot 실행 구성의 활성 프로파일에 `local`을 입력한다.
- CLI: `./gradlew bootRun --args='--spring.profiles.active=local'`로 실행한다.

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