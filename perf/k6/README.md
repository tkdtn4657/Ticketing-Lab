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

이 시나리오의 기대 결과는 `hold_success=1`, `hold_rejected=99`, `hold_unexpected=0`이다.
`hold_rejected`는 좌석별 queue 초과, Redis pre-lock 충돌, DB 상태 충돌처럼 최종적으로 선점에 실패한 요청을 합친 값이다. 현재 queue 초과와 pre-lock 충돌은 모두 `409 Conflict`로 반환한다.
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
  -e SETUP_SETTLE_SECONDS=20 `
  -e K6_PROMETHEUS_RW_SERVER_URL=http://prometheus:9090/api/v1/write `
  grafana/k6 run -o experimental-prometheus-rw /scripts/load/hold-seat-race.js
```

이 시나리오의 기대 결과는 `hold_success=1`, `hold_rejected=999`, `hold_unexpected=0`이다.
`hold_conflict`는 좌석별 queue, Redis pre-lock, DB 중 어느 단계든 이미 선점 경쟁에서 밀린 것으로 판단해 `409`로 실패한 수이다.
같은 좌석에 요청이 몰릴 때 좌석별 queue가 순간 진입량을 제한하고, Redis pre-lock이 DB 진입 전 추가 요청을 먼저 차단하며, 최종 정합성은 DB row lock/optimistic lock이 보호하는지 확인한다.
`SETUP_TIMEOUT`은 테스트 전 사용자 생성/로그인 준비 단계 제한 시간이고, `MAX_DURATION`은 실제 Hold 동시 요청 실행 단계 제한 시간이다.
`SETUP_BATCH_SIZE`는 사용자 회원가입/로그인을 몇 개씩 묶어서 병렬 요청할지 정한다. 값을 키우면 준비 단계는 빨라질 수 있지만, BCrypt 해시/검증이 한꺼번에 실행되어 백엔드 CPU가 더 크게 튈 수 있다.
`SETUP_SETTLE_SECONDS`는 사용자 준비 부하와 실제 Hold 경쟁 부하가 Grafana에서 섞이지 않도록 setup 종료 후 대기하는 시간이다.

500 / 1000 / 3000 / 5000 / 10000명 정책 검증 매트릭스:

```powershell
$raceUsers = @(500, 1000, 3000, 5000, 10000)

foreach ($users in $raceUsers) {
  docker run --rm `
    --network ticketing-lab-full_ticketing `
    -v "$($PWD.Path)\backend\perf\k6:/scripts" `
    -e BASE_URL=http://frontend:8080 `
    -e RACE_USERS=$users `
    -e MAX_DURATION=3m `
    -e SETUP_TIMEOUT=15m `
    -e SETUP_BATCH_SIZE=100 `
    -e SETUP_SETTLE_SECONDS=20 `
    -e K6_REPORT_DIR=/scripts/results `
    -e K6_PROMETHEUS_RW_SERVER_URL=http://prometheus:9090/api/v1/write `
    grafana/k6 run -o experimental-prometheus-rw /scripts/load/hold-seat-race.js
}
```

위 매트릭스는 고유 사용자 토큰을 생성해 같은 좌석에 동시에 Hold를 시도한다. 로컬 PC에서 10000명까지 실행하면 부하 생성기, Docker Desktop, host network 한계가 먼저 드러날 수 있으므로, `10000명` 결과는 정상 응답률과 네트워크 오류 위치를 함께 봐야 한다.

샤드형 동일 좌석 Hold 경쟁:

단일 k6 Docker 컨테이너가 3000 VU 이상에서 먼저 한계에 닿으면 여러 k6 컨테이너로 부하 생성기를 나눌 수 있다. 이때 각 컨테이너가 fixture를 따로 만들면 같은 좌석 경쟁이 아니므로, 반드시 같은 `SHOW_ID`, `SEAT_ID`, `START_AT_EPOCH_MS`를 공유해야 한다.

샤드용 스크립트는 아래 파일이다.

```text
backend/perf/k6/load/hold-seat-race-shard.js
```

워크스페이스 루트의 helper를 쓰면 공통 fixture 생성, shard별 k6 Docker 실행, 공통 시작 시각 전달을 한 번에 처리한다.

```powershell
powershell -ExecutionPolicy Bypass -File .\.loadtest\Invoke-K6HoldRaceSharded.ps1 `
  -TotalUsers 5000 `
  -Shards 5 `
  -StartDelaySeconds 300 `
  -SetupBatchSize 100 `
  -SetupTimeout 30m `
  -MaxDuration 5m
```

이 예시는 5개 k6 컨테이너가 각각 1000 VU를 준비한 뒤 같은 좌석을 동시에 요청한다. Docker Desktop 전체 메모리가 부족하면 shard 수를 늘려도 전체 실행은 실패할 수 있다. 로컬 Docker Desktop 메모리는 최소 8GB, 가능하면 16GB 이상을 권장한다.

Redis pre-lock은 기본적으로 `HOLD_PRE_LOCK_ENABLED=true`, `HOLD_PRE_LOCK_TTL=60s`로 동작한다.
좌석별 queue는 기본적으로 `HOLD_SEAT_QUEUE_ENABLED=true`, `HOLD_SEAT_QUEUE_MAX_PER_SEAT=100`, `HOLD_SEAT_QUEUE_TTL=30s`로 동작한다.
Hold API fast-fail은 실험용 옵션이며 기본적으로 `HOLD_FAST_FAIL_ENABLED=false`, `HOLD_FAST_FAIL_MAX_CONCURRENT=50`으로 동작한다.
Before/After 비교가 필요하면 `.env`에서 `HOLD_PRE_LOCK_ENABLED=false` 또는 `HOLD_SEAT_QUEUE_ENABLED=false`로 내리고 백엔드를 재기동한 뒤 같은 k6 명령을 다시 실행한다.
fast-fail 전후 비교가 필요하면 `.env`에서 `HOLD_FAST_FAIL_ENABLED=true`로 올리고 같은 방식으로 비교한다. 다만 채택한 기본 정책은 fast-fail이 아니라 좌석별 queue + Redis pre-lock이다.

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
- `hold_conflict`: 동일 좌석 Hold 충돌 수. 좌석별 queue 초과, Redis pre-lock 실패, DB 상태 충돌 등으로 `409` 실패한 수이다.
- `hold_fast_fail`: Hold API fast-fail bulkhead에서 `429`로 즉시 거절한 수이다. 기본 채택 정책에서는 보통 `0`이어야 한다.
- `hold_rejected`: `hold_conflict + hold_fast_fail` 성격의 최종 거절 수. 같은 좌석 1개 경쟁에서는 `RACE_USERS - 1` 이상이어야 한다.
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
- `ticketing_hold_fast_fail_rejected_total`: Hold API bulkhead에서 빠르게 거절한 요청 수이다.
- `ticketing_hold_fast_fail_in_use`: 현재 Hold API bulkhead에서 사용 중인 처리 슬롯 수이다.

동일 좌석 1000명 경쟁에서 `hold_success=1`, `hold_rejected=999`, `hold_unexpected=0`이면서 `PostgreSQL Tuple Lock Waits Peak`가 `1` 이상이면, 정합성은 맞고 병목은 같은 좌석 row lock 경합으로 해석한다.
좌석별 queue + Redis pre-lock 적용 후에는 같은 시나리오에서 `PostgreSQL Tuple Lock Waits Peak`, `Hikari Pending Connections`, `Hikari Pool Usage`가 내려가고 `Redis Commands`가 올라가는지 비교한다.
Spring fast-fail 적용 후에는 `hold_fast_fail`과 `ticketing_hold_fast_fail_rejected_total`이 올라가는 대신 Redis/DB 지표가 더 안정되는지 확인한다. 다만 현재 채택한 1차 보호선은 좌석별 queue + Redis pre-lock이고, 3000명 이상 순간 유입의 추가 방어선은 Nginx/API Gateway/admission service에서 검증한다.

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

