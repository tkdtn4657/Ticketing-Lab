# Android 앱 시작 가이드

## 어디를 열어야 하나
- Android Studio에서 저장소 전체가 아니라 `android-app/` 폴더를 연다.
- 백엔드 Spring 프로젝트와 Android 프로젝트는 같은 저장소 안에 있지만 Gradle 프로젝트는 분리되어 있다.

## 현재 Android 앱 상태
- 기술 스택: `Kotlin + Jetpack Compose + Hilt + Retrofit + DataStore`
- 현재 연결된 화면:
  - Splash
  - Login
  - Signup
  - EventList
  - EventDetail
  - ShowAvailability
- 다음 단계 확장 예정:
  - Hold
  - Reservation
  - Payment
  - Ticket
  - Checkin
  - Admin

## 처음 설정 순서
1. Android Studio 설치
2. Android SDK 설치
3. `android-app/local.properties.example` 참고해서 `android-app/local.properties` 생성
4. `local.properties`에 `sdk.dir` 과 `ticketingApiBaseUrl` 입력
5. 백엔드 서버 실행
6. Android 앱 Gradle Sync
7. 에뮬레이터 또는 실제 기기 실행

예시:
```properties
sdk.dir=C:\Users\사용자명\AppData\Local\Android\Sdk
ticketingApiBaseUrl=http://10.0.2.2:9090/
```

## API 주소 규칙
- Android 에뮬레이터에서 로컬 서버 접속 시 `http://10.0.2.2:9090/` 사용
- `localhost`를 쓰면 Android 에뮬레이터 자기 자신을 가리키므로 백엔드에 접속되지 않는다.
- 실제 단말 테스트 시에는 PC의 사설 IP로 변경해야 한다.

## 어디부터 보면 좋은가
- 앱 진입: `android-app/app/src/main/java/com/ticketinglab/android/app/MainActivity.kt`
- 네비게이션: `android-app/app/src/main/java/com/ticketinglab/android/app/navigation/AppNavHost.kt`
- 인증 화면: `android-app/app/src/main/java/com/ticketinglab/android/feature/auth`
- 이벤트 화면: `android-app/app/src/main/java/com/ticketinglab/android/feature/event`
- 회차 가용성 화면: `android-app/app/src/main/java/com/ticketinglab/android/feature/show`
- 네트워크 설정: `android-app/app/src/main/java/com/ticketinglab/android/core/network/NetworkModule.kt`

## 패키지 분리 기준
현재는 `단일 app 모듈 + 기능별 패키지 분리` 방식이다.

예시:
- `feature/auth`
- `feature/event`
- `feature/show`
- `core/network`
- `core/datastore`

이 방식은 Android를 처음 시작할 때 가장 관리하기 쉽고, 기능이 커지면 나중에 멀티모듈로 옮기기도 좋다.

## 트러블슈팅
- `SDK location not found`
  - `android-app/local.properties`에 실제 SDK 경로를 넣어야 한다.
  - 예: `sdk.dir=C:\Users\사용자명\AppData\Local\Android\Sdk`
- `localhost`로는 API 호출이 안 됨
  - 에뮬레이터에서는 `10.0.2.2`를 사용해야 한다.
- 로그인은 되는데 이후 API가 401
  - access token 저장, refresh 재발급, Bearer 헤더 추가 흐름을 먼저 확인한다.

## 지금 단계에서 중요한 점
- 패키지는 나눈다.
- Gradle 멀티모듈은 아직 하지 않는다.
- 먼저 Auth / Event / Show 흐름을 안정적으로 붙인다.
- 그 다음 Hold / Reservation / Payment 를 추가한다.

## 백엔드와 함께 작업하는 팁
- 백엔드 Swagger 문서 경로: `/docs/swagger-ui.html`
- Android DTO는 Swagger와 실제 응답을 같이 보면서 맞춘다.
- 인증이 필요한 API는 access token 저장/재발급 흐름을 먼저 붙이고 나서 화면을 늘리는 것이 좋다.