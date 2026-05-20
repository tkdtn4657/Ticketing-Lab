# Hold 좌석 선점 정책 k6 재측정 리포트

작성일: 2026-05-20

## 1. 재측정 배경

이전 4개 정책 비교는 PowerShell + .NET `HttpClient` 기반 커스텀 러너로 수행했다. 동시 요청 재현에는 사용할 수 있었지만, 표준 부하 테스트 결과로 남기기에는 한계가 있다.

따라서 동일 정책 매트릭스를 k6로 다시 실행했다.

비교 대상은 동일하다.

| 케이스 | 브랜치 | 정책 |
| --- | --- | --- |
| main-db-lock | `main` | 기존 DB lock 기반 |
| seat-queue-only | `codex/seat-queue-limit` | 좌석별 queue만 적용 |
| redis-prelock | `origin/codex/redis-seat-prelock` | Redis pre-lock 적용 |
| seat-queue-plus-prelock | `codex/redis-prelock-seat-queue-limit` | 좌석별 queue + Redis pre-lock 적용 |

## 2. k6 실행 방식

사용한 k6 시나리오는 `backend/perf/k6/load/hold-seat-race.js`이다.

각 실행은 다음 조건으로 수행했다.

- executor: `per-vu-iterations`
- VU 수: `RACE_USERS`
- 각 VU 반복 수: 1회
- 요청 대상: 같은 show의 같은 seat 1개
- 사용자: k6 setup 단계에서 `RACE_USERS` 수만큼 회원가입/로그인 후 각 VU가 고유 토큰 사용
- 기대값: `hold_success=1`, `hold_rejected=RACE_USERS-1`, `hold_unexpected=0`

브랜치별로 Docker Compose를 재생성했다.

```text
git switch <branch>
docker compose down -v --remove-orphans
docker compose up -d --build
docker run grafana/k6 run /scripts/load/hold-seat-race.js
```

부하 생성은 k6가 수행했고, PowerShell은 브랜치 전환과 Docker/k6 실행을 반복하기 위한 오케스트레이션에만 사용했다.

결과 위치:

```text
backend/perf/k6/results/policy-matrix-k6/
```

## 3. 유효한 k6 결과

3000명 이상은 로컬 k6 실행기 한계로 무효 처리했다. 따라서 아래 표는 k6 JSON summary가 정상 생성된 500명, 1000명 결과만 포함한다.

응답 시간은 `http_req_duration{scenario:hold_same_seat_race}` 기준이다. setup 단계의 회원가입/로그인 요청이 섞이지 않도록 실제 Hold 경쟁 시나리오 태그만 사용했다.

| 정책 | VU | 성공 | 409 | 429 | 거절 합계 | 예상외 | 실패율 | 평균 ms | p95 ms | p99 ms | 최대 ms |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| main-db-lock | 500 | 1 | 499 | 0 | 499 | 0 | 0.00% | 2239.53 | 3325.50 | 3380.12 | 3430.61 |
| redis-prelock | 500 | 1 | 499 | 0 | 499 | 0 | 0.00% | 523.16 | 590.50 | 1051.32 | 1055.88 |
| seat-queue-only | 500 | 1 | 499 | 0 | 499 | 0 | 0.00% | 2425.51 | 3521.93 | 3561.32 | 3676.36 |
| seat-queue-plus-prelock | 500 | 1 | 499 | 0 | 499 | 0 | 0.00% | 426.14 | 525.93 | 566.64 | 784.68 |
| main-db-lock | 1000 | 1 | 999 | 0 | 999 | 0 | 0.00% | 2199.94 | 3136.51 | 3218.58 | 3256.21 |
| redis-prelock | 1000 | 1 | 999 | 0 | 999 | 0 | 0.00% | 235.75 | 448.53 | 481.53 | 753.15 |
| seat-queue-only | 1000 | 1 | 999 | 0 | 999 | 0 | 0.00% | 2538.64 | 4095.26 | 4183.24 | 4243.30 |
| seat-queue-plus-prelock | 1000 | 1 | 999 | 0 | 999 | 0 | 0.00% | 302.74 | 415.14 | 465.14 | 493.45 |

## 4. 3000명 이상 결과 무효 처리 사유

3000, 5000, 10000 VU도 모두 실행을 시도했다. 그러나 k6 summary JSON이 생성되지 않았고, Docker/k6 프로세스가 먼저 종료됐다.

실행 로그 기준 결과는 다음과 같다.

| 정책 | 3000 | 5000 | 10000 |
| --- | ---: | ---: | ---: |
| main-db-lock | exit 125 | exit 137 | exit 125 |
| seat-queue-only | exit 137 | exit 137 | exit 137 |
| redis-prelock | exit 137 | exit 125 | exit 137 |
| seat-queue-plus-prelock | exit 125 | exit 137 | exit 137 |

해석:

- `exit 137`: 컨테이너가 강제 종료된 상태로, 일반적으로 OOM kill 가능성이 높다.
- `exit 125`: Docker run/container wait 단계 실패이다. 실행 중 `unexpected EOF` 로그가 함께 관측됐다.

Docker 환경 확인 결과:

```text
Docker CPUs = 12
Docker Memory = 약 2GB
```

이 환경에서 k6 Docker 컨테이너가 3000~10000 VU를 안정적으로 생성하지 못했다. 따라서 3000명 이상 결과는 애플리케이션 정책 비교값으로 사용하면 안 된다.

## 5. k6 기준 해석

500명, 1000명 k6 결과는 이전 커스텀 러너 결과와 같은 방향을 보인다.

### main-db-lock

정합성은 맞지만 응답 시간이 길다.

- 1000 VU p95: 3136.51ms
- 모든 요청이 DB lock 경쟁까지 들어가므로 tail latency가 커진다.

### seat-queue-only

단독 사용은 좋지 않다.

- 1000 VU p95: 4095.26ms
- queue 진입 제한 비용은 추가되지만, queue를 통과한 요청은 여전히 DB lock 경합으로 몰린다.

### redis-prelock

500~1000 VU 기준으로 매우 빠르다.

- 1000 VU p95: 448.53ms
- DB에 들어가기 전 대부분의 중복 선점 요청을 Redis에서 빠르게 차단한다.

### seat-queue-plus-prelock

채택 후보로 유지할 수 있다.

- 500 VU 기준 가장 빠름: p95 525.93ms
- 1000 VU 기준 Redis pre-lock 단독과 비슷하거나 더 안정적인 tail을 보임: p95 415.14ms
- queue는 5000명 이상 순간 폭주를 고려한 admission control 역할을 담당한다.

## 6. 중요한 결론

이번 k6 재측정으로 확인한 것은 두 가지이다.

1. 500~1000 VU 범위에서는 `Redis pre-lock` 계열이 DB lock 단독보다 확실히 빠르다.
2. 현재 로컬 Docker Desktop 2GB 메모리 환경에서는 k6로 3000~10000 VU 완전동시 테스트를 정확히 수행할 수 없다.

따라서 `5000명 완전동시 요청을 k6로 정확히 검증했다`고 말하면 안 된다. 정확한 표현은 다음과 같다.

```text
k6 로컬 Docker 실행 기준으로는 1000 VU까지 유효 결과를 얻었다.
3000 VU 이상은 k6 load generator가 먼저 한계에 도달해 무효 처리했다.
5000명 이상 검증은 더 큰 부하 생성 환경 또는 분산 k6로 재실행해야 한다.
```

## 7. 다음 검증 방식

5000~10000명까지 k6로 제대로 검증하려면 아래 중 하나가 필요하다.

### 7.1 Docker Desktop 리소스 증설

Docker Desktop 메모리를 최소 8GB, 가능하면 16GB 이상으로 올린 뒤 재실행한다.

단일 PC에서 재시도할 때도 `10000 VU`는 여전히 불안정할 수 있다.

### 7.2 Native k6 설치 후 실행

Docker 컨테이너 메모리 오버헤드를 줄이기 위해 Windows 또는 WSL에 k6를 직접 설치하고 실행한다.

다만 native k6도 단일 장비의 CPU, 메모리, socket 한계를 넘을 수는 없다.

### 7.3 분산 k6

가장 정확한 방식이다.

예시:

- 5개 부하 생성기 x 1000 VU = 5000 VU
- 10개 부하 생성기 x 1000 VU = 10000 VU
- k6 cloud, Grafana Cloud k6, Kubernetes k6-operator, 또는 여러 머신에서 synchronized start 사용

이때 setup 단계의 회원가입/로그인은 본 테스트와 분리하는 것이 좋다. 실제 Hold 경쟁 측정에는 이미 준비된 토큰 풀을 사용해야 한다.

## 8. 현재 채택 판단

k6 재측정 후에도 채택 방향은 바뀌지 않는다.

```text
좌석별 queue + Redis pre-lock + DB row lock 최종 보호
```

다만 5000명 이상 수치를 k6 공식 결과로 주장하려면, 로컬 단일 Docker 환경이 아니라 분산 k6 환경에서 다시 측정해야 한다.

