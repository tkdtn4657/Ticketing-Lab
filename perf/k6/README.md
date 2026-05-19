# k6 성능 / 동시성 테스트

## 목적

- 공개 조회 API의 응답 시간과 에러율을 확인한다.
- 동일 좌석 Hold 경쟁에서 정확히 한 요청만 성공하는지 확인한다.
- 결제 멱등성 요청에서 결제/티켓 중복 생성이 없는지 확인한다.

## 실행 전 조건

루트 워크스페이스에서 Docker Compose가 떠 있어야 한다.

```powershell
cd C:\Users\tkdtn\IdeaProjects\ticketing-lab-full
docker compose --env-file .env up -d --build
```

## Docker로 실행

로컬에 `k6`를 설치하지 않아도 Docker로 실행할 수 있다.

```powershell
cd C:\Users\tkdtn\IdeaProjects\ticketing-lab-full

docker run --rm `
  --network ticketing-lab-full_ticketing `
  -v "$($PWD.Path)\backend\perf\k6:/scripts" `
  -e BASE_URL=http://frontend:8080 `
  grafana/k6 run /scripts/load/show-availability.js
```

Grafana/Prometheus에 k6 실행 메트릭까지 보내려면 `-o experimental-prometheus-rw`를 붙인다.

```powershell
docker run --rm `
  --network ticketing-lab-full_ticketing `
  -v "$($PWD.Path)\backend\perf\k6:/scripts" `
  -e BASE_URL=http://frontend:8080 `
  -e K6_PROMETHEUS_RW_SERVER_URL=http://prometheus:9090/api/v1/write `
  grafana/k6 run -o experimental-prometheus-rw /scripts/load/show-availability.js
```

100명 조회 API Burst Smoke:

```powershell
docker run --rm `
  --network ticketing-lab-full_ticketing `
  -v "$($PWD.Path)\backend\perf\k6:/scripts" `
  -e BASE_URL=http://frontend:8080 `
  -e SEAT_COUNT=100 `
  -e VUS=100 `
  -e MAX_DURATION=1m `
  -e K6_PROMETHEUS_RW_SERVER_URL=http://prometheus:9090/api/v1/write `
  grafana/k6 run -o experimental-prometheus-rw /scripts/load/show-availability-burst.js
```

이 시나리오는 `100 VU`가 각자 `1회`씩 `/api/shows/{showId}/availability`를 요청한다.
짧은 조회 폭주가 정상 응답하는지, Grafana에서 latency와 JVM/DB 지표가 튀는지 가볍게 확인하는 smoke 용도이다.

1000명 순간 유입 Burst:

```powershell
docker run --rm `
  --network ticketing-lab-full_ticketing `
  -v "$($PWD.Path)\backend\perf\k6:/scripts" `
  -e BASE_URL=http://frontend:8080 `
  -e SEAT_COUNT=1000 `
  -e VUS=1000 `
  -e MAX_DURATION=1m `
  -e K6_PROMETHEUS_RW_SERVER_URL=http://prometheus:9090/api/v1/write `
  grafana/k6 run -o experimental-prometheus-rw /scripts/load/show-availability-burst.js
```

이 시나리오는 `1000 VU`가 각자 `1회`씩 거의 동시에 `/api/shows/{showId}/availability`를 요청한다.
짧은 시간에 요청이 몰릴 때 Tomcat 요청 스레드, Hikari DB 커넥션 풀, JVM heap, 응답 지연시간이 어떻게 변하는지 확인한다.
본문 JSON 검증까지 포함하려면 `-e VERIFY_BODY=true`를 추가한다. 기본값은 서버 응답 payload 수신과 상태 코드 검증에 집중한다.

동일 좌석 Hold 경쟁:

```powershell
docker run --rm `
  --network ticketing-lab-full_ticketing `
  -v "$($PWD.Path)\backend\perf\k6:/scripts" `
  -e BASE_URL=http://frontend:8080 `
  -e RACE_USERS=10 `
  grafana/k6 run /scripts/load/hold-seat-race.js
```

Prometheus remote write 포함:

```powershell
docker run --rm `
  --network ticketing-lab-full_ticketing `
  -v "$($PWD.Path)\backend\perf\k6:/scripts" `
  -e BASE_URL=http://frontend:8080 `
  -e RACE_USERS=10 `
  -e K6_PROMETHEUS_RW_SERVER_URL=http://prometheus:9090/api/v1/write `
  grafana/k6 run -o experimental-prometheus-rw /scripts/load/hold-seat-race.js
```

100명 동일 좌석 Hold Smoke:

```powershell
docker run --rm `
  --network ticketing-lab-full_ticketing `
  -v "$($PWD.Path)\backend\perf\k6:/scripts" `
  -e BASE_URL=http://frontend:8080 `
  -e RACE_USERS=100 `
  -e MAX_DURATION=1m `
  -e SETUP_TIMEOUT=3m `
  -e SETUP_BATCH_SIZE=50 `
  -e K6_PROMETHEUS_RW_SERVER_URL=http://prometheus:9090/api/v1/write `
  grafana/k6 run -o experimental-prometheus-rw /scripts/load/hold-seat-race.js
```

이 시나리오의 기대 결과는 `hold_success=1`, `hold_conflict=99`, `hold_unexpected=0`이다.
실제 좌석 선점 동시성 smoke는 이 명령을 우선 사용한다.

1000명 동일 좌석 Hold 순간 경쟁:

```powershell
docker run --rm `
  --network ticketing-lab-full_ticketing `
  -v "$($PWD.Path)\backend\perf\k6:/scripts" `
  -e BASE_URL=http://frontend:8080 `
  -e RACE_USERS=1000 `
  -e MAX_DURATION=2m `
  -e SETUP_TIMEOUT=5m `
  -e SETUP_BATCH_SIZE=100 `
  -e K6_PROMETHEUS_RW_SERVER_URL=http://prometheus:9090/api/v1/write `
  grafana/k6 run -o experimental-prometheus-rw /scripts/load/hold-seat-race.js
```

이 시나리오의 기대 결과는 `hold_success=1`, `hold_conflict=999`, `hold_unexpected=0`이다.
같은 좌석에 요청이 몰릴 때 DB row lock/optimistic lock 충돌이 정상적으로 `409 Conflict`로 흘러가는지 확인한다.
`SETUP_TIMEOUT`은 테스트 전 사용자 생성/로그인 준비 단계 제한 시간이고, `MAX_DURATION`은 실제 Hold 동시 요청 실행 단계 제한 시간이다.
`SETUP_BATCH_SIZE`는 사용자 회원가입/로그인을 몇 개씩 묶어서 병렬 요청할지 정한다. 값을 키우면 준비 단계는 빨라질 수 있지만, BCrypt 해시/검증이 한꺼번에 실행되어 백엔드 CPU가 더 크게 튈 수 있다.

결제 멱등성:

```powershell
docker run --rm `
  --network ticketing-lab-full_ticketing `
  -v "$($PWD.Path)\backend\perf\k6:/scripts" `
  -e BASE_URL=http://frontend:8080 `
  -e PAYMENT_REQUESTS=10 `
  grafana/k6 run /scripts/load/payment-idempotency.js
```

Prometheus remote write 포함:

```powershell
docker run --rm `
  --network ticketing-lab-full_ticketing `
  -v "$($PWD.Path)\backend\perf\k6:/scripts" `
  -e BASE_URL=http://frontend:8080 `
  -e PAYMENT_REQUESTS=10 `
  -e K6_PROMETHEUS_RW_SERVER_URL=http://prometheus:9090/api/v1/write `
  grafana/k6 run -o experimental-prometheus-rw /scripts/load/payment-idempotency.js
```

## 로컬 k6로 실행

로컬에 `k6`가 설치되어 있다면 아래처럼 실행한다.

```powershell
$env:BASE_URL="http://127.0.0.1:18080"
k6 run .\backend\perf\k6\load\show-availability.js
```

## 결과 확인 기준

k6 콘솔에서 아래 값을 본다.

- `http_req_duration`: 평균, p95, p99 응답 시간
- `http_req_failed`: HTTP 실패율
- `checks`: 스크립트 검증 통과율
- `availability_burst_ok`: 순간 유입 Burst에서 정상 응답한 요청 수
- `hold_success`: 동일 좌석 Hold 성공 수. `1`이어야 한다.
- `hold_conflict`: 동일 좌석 Hold 충돌 수. `RACE_USERS - 1` 이상이어야 한다.
- `payment_approved`: 같은 멱등키 결제 성공 응답 수. `PAYMENT_REQUESTS`와 같아야 한다.
- `ticket_count_ok`: 결제 멱등성 테스트 후 티켓이 1장만 발급됐는지 확인한다.

## Grafana에서 락 경합 확인

동일 좌석 Hold 경쟁 테스트를 실행할 때는 `Ticketing Lab Overview`에서 아래 패널을 함께 본다.

- `PostgreSQL Lock Waits 1m Max`: PostgreSQL 세션이 최근 1분 안에 어떤 lock을 기다렸는지 보여준다. `Lock tuple`이 올라가면 row lock 대기이다.
- `PostgreSQL Tuple Lock Waits Now`: 현재 tuple lock 대기 수를 단일 값으로 보여준다. 테스트가 끝났다면 `0`이어야 한다.
- `PostgreSQL Tuple Lock Waits Peak`: 선택한 대시보드 시간 범위 안에서 tuple lock 대기가 최대 몇 개였는지 보여준다. 테스트가 끝난 뒤에도 peak 판단에 사용한다.
- `PostgreSQL JDBC Sessions By State`: 백엔드 JDBC 세션의 active/idle 상태를 보여준다.
- `Hikari Pending Connections`: DB 커넥션을 빌리지 못하고 기다리는 요청 수이다.
- `Hikari Pool Usage`: HikariCP 커넥션 풀 사용률이다.

동일 좌석 1000명 경쟁에서 `hold_success=1`, `hold_conflict=999`, `hold_unexpected=0`이면서 `PostgreSQL Tuple Lock Waits Peak`가 `1` 이상이면, 정합성은 맞고 병목은 같은 좌석 row lock 경합으로 해석한다.

## 실행 리포트

각 k6 스크립트는 실행 완료 후 `backend/perf/k6/results/` 아래에 리포트를 남긴다.

- `*.html`: 브라우저에서 보는 요약 리포트
- `*.json`: k6 원본 summary 데이터

Docker 실행 기준으로는 `/scripts/results`가 로컬 `backend/perf/k6/results`에 매핑된다.
리포트 저장 위치를 바꾸고 싶다면 `K6_REPORT_DIR`을 지정한다.

```powershell
docker run --rm `
  --network ticketing-lab-full_ticketing `
  -v "$($PWD.Path)\backend\perf\k6:/scripts" `
  -e BASE_URL=http://frontend:8080 `
  -e RACE_USERS=1000 `
  -e MAX_DURATION=2m `
  -e K6_REPORT_DIR=/scripts/results `
  grafana/k6 run /scripts/load/hold-seat-race.js
```

## DB / Redis 관측

PostgreSQL 세션과 락:

```powershell
docker compose --env-file .env exec postgres psql -U ticketing -d ticketing -c "select pid, state, wait_event_type, wait_event, query from pg_stat_activity where datname = 'ticketing';"
docker compose --env-file .env exec postgres psql -U ticketing -d ticketing -c "select locktype, mode, granted, relation::regclass from pg_locks where database = (select oid from pg_database where datname = 'ticketing');"
```

Redis 상태:

```powershell
docker compose --env-file .env exec redis redis-cli INFO stats
docker compose --env-file .env exec redis redis-cli INFO commandstats
```

