# Bimil (비밀)

> **비밀번호를 저장하지 않는** 비밀번호 힌트 관리 앱

Bimil은 실제 비밀번호를 저장하지 않고, 사용자의 기억을 돕는 **힌트만 관리**하는 오프라인 우선 앱입니다.

## 핵심 원칙

| 원칙 | 설명 |
|------|------|
| **제로 스토리지** | 실제 비밀번호는 절대 저장하지 않음 |
| **오프라인 퍼스트** | 인터넷 없이 모든 핵심 기능 동작 |
| **제로 설정** | 계정 생성, 로그인 불필요 |
| **프라이버시 중심** | 사용자 데이터 수집/전송 없음 |

## 지원 플랫폼

- **Android** 7.0+ (API 24)
- **Desktop** (Windows, macOS, Linux)
- **iOS** 15.0+ (예정)

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| **언어** | Kotlin 2.0.21 |
| **UI 프레임워크** | Compose Multiplatform 1.7.0 |
| **아키텍처** | Clean Architecture (Domain/Data/Presentation) |
| **DI** | Koin 4.0.0 |
| **로컬 DB** | SQLDelight 2.0.2 |
| **암호화** | SQLCipher (Android), AES-256 |
| **네비게이션** | Jetpack Navigation Compose 2.8.0 |
| **비동기** | Kotlin Coroutines 1.8.1 + Flow |

---

## 프로젝트 구조

```
bimil/
├── shared/                     # KMP 공통 모듈 (약 70% 코드 공유)
│   └── src/
│       ├── commonMain/         # 공통 코드
│       │   └── kotlin/.../
│       │       ├── domain/     # 도메인 레이어
│       │       │   ├── model/      # Account, Category, Settings 등
│       │       │   ├── repository/ # Repository 인터페이스
│       │       │   └── usecase/    # 비즈니스 로직
│       │       ├── data/       # 데이터 레이어
│       │       │   ├── repository/ # Repository 구현체
│       │       │   ├── database/   # SQLDelight DB
│       │       │   ├── encryption/ # 암호화 서비스
│       │       │   └── backup/     # 백업/복원
│       │       ├── presentation/   # UI 레이어
│       │       │   ├── screen/     # HomeScreen, SettingsScreen 등
│       │       │   ├── component/  # 재사용 컴포넌트
│       │       │   ├── viewmodel/  # ViewModels
│       │       │   ├── theme/      # Material 3 테마
│       │       │   └── navigation/ # 네비게이션 설정
│       │       └── di/         # Koin 모듈
│       ├── androidMain/        # Android 전용 코드
│       └── desktopMain/        # Desktop 전용 코드
│
├── androidApp/                 # Android 앱 모듈
│   └── src/main/
│       ├── kotlin/.../
│       │   ├── BimilApplication.kt  # Application 클래스 (Koin 초기화)
│       │   └── MainActivity.kt      # 메인 액티비티
│       ├── res/                     # Android 리소스
│       └── AndroidManifest.xml
│
├── desktopApp/                 # Desktop 앱 모듈
│   └── src/desktopMain/
│       └── kotlin/.../
│           └── Main.kt         # 데스크톱 진입점
│
├── gradle/
│   └── libs.versions.toml      # 버전 카탈로그
│
└── build.gradle.kts            # 루트 빌드 설정
```

---

## 빌드 및 실행 방법

### 사전 요구사항

- **JDK 17** 이상
- **Android Studio** Hedgehog 이상 (Android 빌드 시)
- **Gradle 8.7**

### Android 앱 실행

#### Android Studio에서 실행
1. Android Studio에서 프로젝트 열기
2. `androidApp` 모듈 선택
3. 에뮬레이터 또는 실제 기기 연결
4. **Run** 버튼 클릭

#### 커맨드 라인에서 빌드

```bash
# Debug APK 빌드
./gradlew :androidApp:assembleDebug

# Release APK 빌드
./gradlew :androidApp:assembleRelease

# 빌드된 APK 위치
# Debug: androidApp/build/outputs/apk/debug/androidApp-debug.apk
# Release: androidApp/build/outputs/apk/release/androidApp-release.apk
```

#### 기기에 직접 설치

```bash
# Debug 버전 설치 및 실행
./gradlew :androidApp:installDebug

# 연결된 기기 확인
adb devices
```

---

### Desktop 앱 실행

#### IDE에서 실행

```bash
# Gradle task로 실행
./gradlew :desktopApp:run
```

#### 배포 가능한 패키지 생성

```bash
# 현재 OS용 배포 패키지 생성
./gradlew :desktopApp:packageDistributionForCurrentOS

# Windows: MSI 설치 파일 생성
./gradlew :desktopApp:packageMsi

# macOS: DMG 파일 생성
./gradlew :desktopApp:packageDmg

# Linux: DEB 패키지 생성
./gradlew :desktopApp:packageDeb
```

배포 패키지 위치: `desktopApp/build/compose/binaries/main/`

---

### 전체 프로젝트 빌드

```bash
# 전체 빌드 (Android + Desktop)
./gradlew build -x :shared:verifyCommonMainBimilDatabaseMigration

# 클린 빌드
./gradlew clean build -x :shared:verifyCommonMainBimilDatabaseMigration
```

> **참고**: Windows 환경에서 SQLite migration 검증 시 네이티브 라이브러리 이슈가 있을 수 있어 `-x` 옵션으로 제외합니다.

---

## 주요 기능

### 계정 관리
- 서비스별 계정 정보 등록 (서비스명, 사용자명, URL)
- 카테고리별 분류 (소셜, 금융, 쇼핑 등)
- 즐겨찾기 기능
- 검색 및 필터링

### 비밀번호 힌트
- 로그인 유형 선택 (Password / SSO)
- 비밀번호 요구사항 기록 (최소 길이, 특수문자, 대소문자 등)
- 개인 힌트 메모

### SSO 제공자 지원
- 지역별 SSO 프리셋 (한국: 카카오, 네이버, 토스 등)
- 글로벌: Google, Apple, Facebook, Microsoft, GitHub

### 보안
- PIN 잠금
- 생체 인증 (Android)
- 로컬 데이터 암호화

### 백업/복원
- 암호화된 백업 파일 생성
- 백업에서 복원

---

## 개발 가이드

### 새로운 화면 추가

1. `shared/src/commonMain/.../presentation/screen/`에 Screen 컴포저블 생성
2. `shared/src/commonMain/.../presentation/viewmodel/`에 ViewModel 생성
3. `shared/src/commonMain/.../presentation/navigation/Navigation.kt`에 라우트 추가
4. `shared/src/commonMain/.../di/Modules.kt`에 ViewModel 등록

### 데이터베이스 스키마 변경

1. `shared/src/commonMain/sqldelight/`의 `.sq` 파일 수정
2. `./gradlew :shared:generateCommonMainBimilDatabaseInterface` 실행
3. Repository 구현체 업데이트

---

## 라이선스

이 프로젝트는 개인 프로젝트입니다.

---

## 관련 문서

- [PRD.md](./PRD.md) - 제품 요구사항 문서
- [architecture.md](./architecture.md) - 시스템 아키텍처 문서
