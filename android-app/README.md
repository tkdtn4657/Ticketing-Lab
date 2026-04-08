# Ticketing Lab Android

## 개요
- 이 폴더는 Ticketing Lab 백엔드와 연동하는 Android 앱 프로젝트다.
- 현재는 `Kotlin + Jetpack Compose + Hilt + Retrofit + DataStore` 기준의 초기 앱 뼈대가 구성되어 있다.
- 1차로 연결된 화면은 `Splash -> Login -> Signup -> EventList -> EventDetail -> ShowAvailability` 이다.
- Hold, Reservation, Payment, Ticket, Checkin, Admin 은 다음 단계 확장 대상으로 남겨두었다.

## 왜 이렇게 시작하나
- Android 를 처음 시작할 때는 `단일 app 모듈 + 기능별 패키지 분리`가 가장 관리하기 쉽다.
- 모듈을 너무 일찍 쪼개면 설정 난이도가 올라가고, 실제 기능 개발보다 빌드 관리가 더 어려워질 수 있다.
- 그래서 지금은 `feature/auth`, `feature/event`, `feature/show` 같은 패키지 분리만 적용하고, 멀티모듈은 나중에 필요할 때 도입한다.

## 현재 기술 스택
- 언어: `Kotlin`
- UI: `Jetpack Compose`
- 상태 관리: `ViewModel + StateFlow`
- DI: `Hilt`
- 네트워크: `Retrofit + OkHttp + Kotlin Serialization`
- 로컬 저장: `DataStore`
- 내비게이션: `Navigation Compose`

## 현재 패키지 구조
- `app`: Application, Activity, Navigation, DI 진입점
- `core`: 공통 디자인 시스템, 네트워크, DataStore, 모델
- `feature/auth`: 로그인, 회원가입, 세션 진입
- `feature/event`: 이벤트 목록, 이벤트 상세
- `feature/show`: 회차 가용성
- `feature/support`: 임시 안내 화면

## 실행 전 준비
1. Android Studio에서 저장소 전체가 아니라 `android-app/` 폴더를 연다.
2. Android SDK를 설치한다.
3. `android-app/local.properties.example`을 참고해 `android-app/local.properties`를 만든다.
4. `local.properties`에 실제 Android SDK 경로와 API base URL을 넣는다.
5. 백엔드 Spring 서버를 실행한다.
6. Android Studio에서 Gradle Sync 후 에뮬레이터 또는 기기를 실행한다.

예시:
```properties
sdk.dir=C:\Users\사용자명\AppData\Local\Android\Sdk
ticketingApiBaseUrl=http://10.0.2.2:9090/
```

## API 연동 기준
- base URL은 `BuildConfig.API_BASE_URL`
- 인증 헤더는 `Authorization: Bearer {accessToken}`
- refresh token은 DataStore에 저장한다.
- 401 발생 시 `RefreshTokenAuthenticator`가 refresh API를 호출하고, 성공하면 원 요청을 1회 재시도한다.

## 주요 경로
- 앱 시작: `app/src/main/java/com/ticketinglab/android/app/MainActivity.kt`
- 앱 테마: `app/src/main/java/com/ticketinglab/android/core/designsystem/theme`
- 네비게이션: `app/src/main/java/com/ticketinglab/android/app/navigation/AppNavHost.kt`
- 네트워크 설정: `app/src/main/java/com/ticketinglab/android/core/network/NetworkModule.kt`
- 세션 저장: `app/src/main/java/com/ticketinglab/android/core/datastore/SessionDataStore.kt`

## 다음 단계 추천
1. Hold / Reservation / Payment 화면과 DTO 추가
2. Ticket / Checkin 화면 추가
3. Admin 기능 추가
4. 디자인 시스템과 공통 컴포넌트 고도화