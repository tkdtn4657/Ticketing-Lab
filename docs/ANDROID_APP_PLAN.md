# Android 앱 설계안

## 결론
- Android 앱은 `Kotlin + Jetpack Compose`로 간다.
- 아키텍처는 `단일 app 모듈 + 기능별 패키지 분리(feature-based)`로 시작한다.
- 화면 계층은 `presentation`, 데이터 계층은 `data`, 공통 코드는 `core`로 분리한다.
- 너무 이른 시점의 멀티모듈 분리는 하지 않는다.
- 현재 백엔드 API 흐름에 맞춰 `Auth -> Event -> Show -> Hold -> Reservation -> Payment -> Ticket` 흐름을 우선 구현한다.
- Admin 기능은 사용자 앱과 같은 프로젝트 안에 두되, `admin` 기능 패키지로 분리한다.

## 왜 Kotlin + Compose 인가
- Android에서 가장 표준적인 조합이다.
- 화면 상태 관리와 API 연동 결과를 표현하기 좋다.
- 예매 앱처럼 목록, 상세, 선택, 결제, 티켓 상태가 자주 바뀌는 흐름에 잘 맞는다.
- 새로 시작할 때 XML View 기반보다 구조를 잡기가 더 단순하다.

## 패키지 분리해야 하나
해야 한다. 다만 처음부터 너무 과하게 나누면 오히려 어렵다.

권장 방향은 아래와 같다.
- `Gradle 모듈 분리`는 아직 하지 않는다.
- 대신 `패키지`는 기능 기준으로 나눈다.
- 각 기능 패키지 안에서 `presentation`, `data`, 필요 시 `domain`으로 나눈다.

즉 지금은 이렇게 가는 게 좋다.
- 좋은 선택: `app` 하나 + 내부 패키지 분리
- 아직 이른 선택: `app`, `core`, `feature-auth`, `feature-event` 같은 멀티모듈

## 확정 기술 스택

### 언어 / UI
- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose

### 상태 관리 / 아키텍처
- MVVM
- `ViewModel + StateFlow`
- 화면 상태는 `UiState`, 사용자 액션은 `Intent` 또는 메서드 호출, 일회성 이벤트는 `UiEffect`로 관리

### DI
- Hilt

### 네트워크
- Retrofit
- OkHttp
- Kotlin Serialization
- OkHttp Interceptor
- OkHttp Authenticator 또는 토큰 재발급 전용 네트워크 처리기

### 로컬 저장소
- DataStore
- Android Keystore 기반 암호화 저장소 적용 검토

### 비동기
- Kotlin Coroutines
- Flow

### 날짜/시간
- `java.time`
- minSdk가 26 미만이면 desugaring 적용

### 이미지 / 기타
- Coil
- QR 스캔이 필요해지면 CameraX는 2차 도입

## 권장 최소 SDK / 타깃
- minSdk: 26 권장
- targetSdk: 최신 안정 버전

이유:
- `java.time` 사용이 편하다.
- Compose와 최신 Android API 사용이 수월하다.
- 내부 MVP 성격에서는 레거시 대응 비용을 줄이는 편이 낫다.

하위 버전 지원이 꼭 필요하면 아래를 추가한다.
- minSdk 24
- core library desugaring 활성화

## 앱 범위
1차 앱 범위는 아래로 잡는다.
- 로그인 / 회원가입
- 이벤트 목록
- 이벤트 상세 + 회차 목록
- 회차 가용성 조회
- 좌석/구역 선택 후 홀드 생성
- 예약 생성
- 결제 승인
- 내 예약 목록
- 내 티켓 목록
- 티켓 상세 / QR 확인
- 관리자 체크인

관리자용 공연장/이벤트/회차/재고 등록 화면도 넣을 수 있지만, 모바일 MVP에서는 우선순위를 낮게 둔다.
관리자 기능을 다 넣어야 한다면 `admin` 패키지로 완전히 분리한다.

## 화면 구조 설계

### 루트 흐름
- `SplashRoute`
- `AuthGraph`
- `MainGraph`
- `AdminGraph`

### AuthGraph
- `LoginScreen`
- `SignupScreen`

### MainGraph
- `EventListScreen`
- `EventDetailScreen`
- `ShowAvailabilityScreen`
- `HoldConfirmScreen`
- `ReservationConfirmScreen`
- `PaymentConfirmScreen`
- `MyReservationsScreen`
- `ReservationDetailScreen`
- `MyTicketsScreen`
- `TicketDetailScreen`
- `ProfileScreen`

### AdminGraph
- `AdminHomeScreen`
- `VenueUpsertScreen`
- `VenueSeatsScreen`
- `VenueSectionsScreen`
- `CreateEventScreen`
- `CreateShowScreen`
- `CreateShowSeatsScreen`
- `CreateSectionInventoriesScreen`
- `CheckinScreen`

## 추천 내비게이션 순서

### 사용자 예매 플로우
1. 로그인
2. 이벤트 목록 조회
3. 이벤트 상세에서 회차 선택
4. 회차 가용성 조회
5. 좌석/구역 선택
6. 홀드 생성
7. 예약 생성
8. 결제 승인
9. 티켓 발급 확인

### 사용자 조회 플로우
1. 내 예약 목록
2. 예약 상세 확인
3. 내 티켓 목록
4. 티켓 상세 / QR 확인

### 관리자 플로우
1. 관리자 로그인
2. 공연장 / 이벤트 / 회차 / 재고 관리
3. 체크인 처리

## 화면별 주요 책임

### LoginScreen
- 이메일/비밀번호 입력
- 로그인 API 호출
- accessToken / refreshToken 저장
- role에 따라 일반 사용자 홈 또는 관리자 홈으로 이동

### EventListScreen
- `GET /api/events`
- 상태 필터 선택 가능
- 카드 리스트 표시

### EventDetailScreen
- `GET /api/events/{eventId}`
- 이벤트 기본 정보와 shows 목록 표시
- 회차 클릭 시 가용성 화면 이동

### ShowAvailabilityScreen
- `GET /api/shows/{showId}/availability`
- 좌석형과 구역형을 섹션 단위로 구분 표시
- 선택 결과를 로컬 상태로 관리
- 다음 버튼 누르면 hold 생성

### HoldConfirmScreen
- 선택한 좌석/구역 확인
- `POST /api/holds`
- `expiresAt` 카운트다운 표시
- hold 생성 성공 시 예약 생성 화면으로 이동

### ReservationConfirmScreen
- `POST /api/reservations`
- 예약 금액, 만료 시각 노출
- 결제 버튼 제공

### PaymentConfirmScreen
- `POST /api/payments/confirm`
- `Idempotency-Key` 생성 후 헤더에 포함
- 중복 클릭 방지
- 성공 시 티켓 화면 이동

### MyReservationsScreen
- `GET /api/me/reservations`
- 상태별 필터
- 페이지 기반 목록

### MyTicketsScreen
- `GET /api/me/tickets`
- ISSUED / USED 상태 표시
- usedAt 표시

### TicketDetailScreen
- ticket 정보 표시
- QR 토큰 표시
- 추후 QR 이미지 생성 가능

### CheckinScreen
- 1차는 수동 입력 기반
- `POST /api/checkin`
- 성공 / 중복 / 미존재 상태 메시지 명확히 노출
- 2차에서 CameraX 연동 검토

## API 연동 기준

### 공통 규칙
- Base URL은 `BuildConfig` 또는 local properties 기반으로 관리한다.
- 모든 인증 API는 `Authorization: Bearer {accessToken}` 헤더를 사용한다.
- `refresh-token` 쿠키는 Android에서 굳이 쓰지 않고, 응답 body의 `refreshToken` 값을 저장해서 사용한다.
- 서버가 refresh token을 body로도 내려주므로 모바일에서는 body 기반 저장 전략이 더 단순하다.

### 토큰 저장 전략
- accessToken: 메모리 + 필요 시 DataStore
- refreshToken: DataStore 저장
- 앱 시작 시 refreshToken이 있으면 자동 로그인 시도

### 401 처리 전략
- accessToken 만료 시 refresh API 호출
- refresh 성공 시 원 요청 1회 재시도
- refresh 실패 시 로그아웃 처리 후 로그인 화면 이동

### 결제 API 규칙
- `Idempotency-Key`는 결제 버튼 클릭 시 UUID로 생성
- 동일 결제 요청 재시도 시 같은 키를 유지
- 성공 화면 전환 전 버튼 중복 탭 방지

### 시간 처리 규칙
- 서버 응답 `LocalDateTime`은 앱 내부에서 `LocalDateTime` 또는 `ZonedDateTime`으로 변환해 사용
- 화면에는 한국 시간 기준 문자열 포맷 적용

### 페이지 API 처리 규칙
- 현재 API는 `page`, `size`, `totalElements`, `totalPages` 구조이므로 우선 직접 페이지네이션 구현
- Paging 3는 2차 도입

## 패키지 구조 권장안

```text
com.ticketinglab.android
├─ app
│  ├─ MainActivity.kt
│  ├─ TicketingLabApp.kt
│  └─ navigation
│     ├─ AppNavHost.kt
│     ├─ AuthGraph.kt
│     ├─ MainGraph.kt
│     └─ AdminGraph.kt
├─ core
│  ├─ common
│  │  ├─ result
│  │  ├─ error
│  │  └─ util
│  ├─ designsystem
│  │  ├─ component
│  │  ├─ theme
│  │  └─ icon
│  ├─ network
│  │  ├─ api
│  │  ├─ interceptor
│  │  ├─ authenticator
│  │  ├─ dto
│  │  └─ NetworkModule.kt
│  ├─ datastore
│  │  ├─ TokenStorage.kt
│  │  └─ SessionDataStore.kt
│  └─ model
│     ├─ auth
│     ├─ event
│     ├─ hold
│     ├─ reservation
│     ├─ payment
│     └─ ticket
├─ feature
│  ├─ auth
│  │  ├─ data
│  │  │  ├─ AuthRepositoryImpl.kt
│  │  │  └─ AuthRemoteDataSource.kt
│  │  ├─ domain
│  │  │  ├─ AuthRepository.kt
│  │  │  ├─ LoginUseCase.kt
│  │  │  ├─ SignupUseCase.kt
│  │  │  └─ RefreshSessionUseCase.kt
│  │  └─ presentation
│  │     ├─ login
│  │     └─ signup
│  ├─ event
│  │  ├─ data
│  │  ├─ domain
│  │  └─ presentation
│  │     ├─ eventlist
│  │     └─ eventdetail
│  ├─ show
│  │  ├─ data
│  │  ├─ domain
│  │  └─ presentation
│  │     └─ availability
│  ├─ hold
│  │  ├─ data
│  │  ├─ domain
│  │  └─ presentation
│  ├─ reservation
│  │  ├─ data
│  │  ├─ domain
│  │  └─ presentation
│  ├─ payment
│  │  ├─ data
│  │  ├─ domain
│  │  └─ presentation
│  ├─ ticket
│  │  ├─ data
│  │  ├─ domain
│  │  └─ presentation
│  └─ admin
│     ├─ data
│     ├─ domain
│     └─ presentation
└─ buildlogic
```

## 처음에는 어디까지 단순화할까
처음부터 모든 기능에 `domain/usecase/repository`를 다 만들면 부담이 크다.
초기에는 아래처럼 시작해도 충분하다.

```text
feature/auth
├─ data
├─ presentation
feature/event
├─ data
├─ presentation
```

그 다음 비즈니스 규칙이 커지는 기능부터 `domain`을 추가한다.

우선 `domain`을 꼭 두면 좋은 기능:
- auth
- hold
- reservation
- payment
- ticket

이유:
- 상태 전이와 규칙이 많다.
- 테스트 가능한 단위로 분리할 가치가 있다.

## 네트워크 레이어 예시

### API 인터페이스 분리
- `AuthApi`
- `EventApi`
- `ShowApi`
- `HoldApi`
- `ReservationApi`
- `PaymentApi`
- `TicketApi`
- `AdminApi`
- `CheckinApi`

### DTO와 UI 모델 분리
반드시 분리하는 것을 권장한다.
- DTO는 서버 응답 그대로 표현한다.
- UI 모델은 화면 표시 목적에 맞게 가공한다.

예시:
- `EventSummaryDto`
- `EventSummary`
- `EventCardUiModel`

처음에는 DTO -> domain model 정도만 해도 충분하다.
UI 전용 모델은 화면이 복잡할 때 추가한다.

## 상태 관리 규칙
각 화면은 아래 3가지를 가진다.
- `UiState`: 화면에 그릴 상태
- `UiAction`: 사용자 액션
- `UiEffect`: 토스트, 네비게이션 같은 일회성 이벤트

예시:
- `EventListUiState(isLoading, events, selectedStatus, errorMessage)`
- `PaymentUiEffect.NavigateToTickets`

## 디자인 방향
예매 앱은 정보량이 많기 때문에 아래 원칙을 권장한다.
- 목록은 카드형으로 단순하게
- 좌석/구역 선택은 화면을 분리해서 복잡도 낮추기
- 결제 직전 요약 화면을 명확하게 두기
- 티켓 화면은 `QR`, `상태`, `회차`, `좌석/구역`을 가장 크게 보이게 하기
- 관리자 화면은 소비자 UI와 완전히 다른 톤으로 분리하기

## 1차 구현 우선순위
1. 앱 기본 세팅
   - Compose
   - Hilt
   - Retrofit
   - DataStore
   - Navigation
2. Auth 구현
   - login
   - signup
   - refresh
   - logout
3. Event / Show 조회 구현
4. Hold / Reservation / Payment 구현
5. Ticket 구현
6. Admin Checkin 구현
7. Admin 등록 화면 구현

## 1차에서 제외해도 되는 것
- 멀티모듈 분리
- 오프라인 캐시
- Paging 3
- CameraX 기반 QR 스캔
- 복잡한 디자인 시스템
- 다국어
- 태블릿 최적화

## 추천 시작 방식
가장 추천하는 시작 방식은 아래다.
- Android Studio에서 Compose 프로젝트 생성
- 패키지는 위 구조대로 먼저 만든다.
- Auth, Event, Show까지만 먼저 붙인다.
- 그 다음 Hold, Reservation, Payment를 이어서 붙인다.
- 마지막에 Ticket, Admin Checkin을 붙인다.

## 제가 추천하는 최종 방향
- 언어: Kotlin
- UI: Jetpack Compose
- 구조: 단일 app 모듈 + feature 패키지 분리
- 상태관리: MVVM + StateFlow
- API: Retrofit + OkHttp + Kotlin Serialization
- DI: Hilt
- 저장소: DataStore

즉, 지금 기준에서는
`Kotlin + Compose + feature 패키지 분리`가 가장 좋은 시작점이다.