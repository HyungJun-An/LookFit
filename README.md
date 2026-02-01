# 👕 LookFit

AI 기반 맞춤형 패션 추천 플랫폼

---

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [핵심 기능](#-핵심-기능)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [설계](#-설계)
- [시작하기](#-시작하기)
- [개발 환경](#-개발-환경)

---

## 🎯 프로젝트 소개

**LookFit**은 AI 기술을 활용하여 사용자의 체형과 선호도에 맞는 패션 아이템을 추천하는 플랫폼입니다.

사용자는 간단한 회원가입과 상품 구매를 통해 개인 맞춤형 스타일링 서비스를 경험할 수 있습니다.

---

## ✨ 핵심 기능

### 1️⃣ 로그인 (Authentication)

- **소셜 로그인 (OAuth2)**
  - Google OAuth2 연동
  - 자동 회원가입 및 로그인
- **JWT 토큰 기반 인증**
  - 안전한 세션 관리
  - 토큰 기반 API 인증

### 2️⃣ 상품 (Product)

- **상품 관리**
  - 상품 조회, 검색
  - 카테고리별 필터링
  - 상품 상세 정보
- **상품 리뷰 & Q&A**
  - 리뷰 작성 및 조회
  - Q&A 게시판
  - 별점 시스템

### 3️⃣ 장바구니 (Cart)

- **장바구니 관리**
  - 상품 추가/삭제
  - 수량 조정
  - 장바구니 조회

### 4️⃣ 결제 (Payment)

- **주문 관리**
  - 주문 생성
  - 주문 내역 조회
  - 배송지 관리
- **상품 입출고 관리**
  - 재고 관리
  - 주문별 입출고 이력

### 5️⃣ AI 피팅 플로우 (AI Fitting)

- **AI 기반 추천 시스템**
  - 사용자 체형 분석
  - 개인 맞춤형 상품 추천
  - 피팅 결과 제공

---

## 🛠 기술 스택

### Backend

- **Framework**: Spring Boot 3.5.9
- **Language**: Java 21
- **Build Tool**: Gradle (Kotlin DSL)
- **Database**: MySQL 8.0
- **ORM**: JPA / Hibernate
- **Query**: QueryDSL 5.0.0

### Security & Authentication

- **Security**: Spring Security
- **OAuth2**: Spring OAuth2 Client
- **JWT**: jjwt 0.12.5

### Development Tools

- **Lombok**: 코드 간소화
- **Actuator**: 모니터링
- **Prometheus**: 메트릭 수집

### (예정) 추가 기술

- Redis (캐싱, 세션 관리)
- AWS S3 (이미지 저장)
- Elasticsearch (검색)

---

## 📁 프로젝트 구조

[//]: # (
```
LookFit/
├── src/
│   ├── main/
│   │   ├── java/com/lookfit/
│   │   │   ├── config/          # 설정 클래스
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── entity/          # JPA 엔티티
│   │   │   │   ├── Member.java
│   │   │   │   ├── Product.java
│   │   │   │   ├── Cart.java
│   │   │   │   ├── Buy.java
│   │   │   │   └── ...
│   │   │   ├── repository/      # 데이터 접근 계층
│   │   │   │   ├── MemberRepository.java
│   │   │   │   └── ...
│   │   │   ├── util/            # 유틸리티 클래스
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── CustomOAuth2UserService.java
│   │   │   │   └── OAuth2SuccessHandler.java
│   │   │   └── LookFitApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-oauth.properties
│   └── test/                    # 테스트 코드
├── build.gradle.kts
├── compose.yaml
├── Lookfit_db.sql
└── README.md
```
)
---

## 📐 설계

### ERD

프로젝트의 데이터베이스 설계는 `Lookfit_db.sql` 파일을 참고하세요.

**주요 엔티티:**
- **Member**: 회원 정보
- **Product**: 상품 정보
- **Cart**: 장바구니
- **Buy**: 주문 정보
- **ProductIo**: 상품 입출고
- **BReview**: 리뷰
- **BQna**: Q&A
- **SocialAccount**: 소셜 계정 정보

### API 명세

> 📝 API 명세서는 별도 문서로 작성 예정입니다.

**주요 API 엔드포인트:**
- `/oauth2/authorization/google` - Google 로그인
- `/login/success` - 로그인 성공 처리
- `/api/v1/**` - 인증이 필요한 API

### 흐름도

#### 로그인 흐름도

```
사용자 → Google OAuth2 → OAuth2SuccessHandler → JWT 토큰 생성 → 클라이언트로 리다이렉트
```

#### 상품 주문 흐름도

```
상품 조회 → 장바구니 추가 → 주문 생성 → 결제 → 상품 입출고 처리
```

---

## 🚀 시작하기

### 필수 요구사항

- Java 21 이상
- MySQL 8.0 이상
- Gradle 8.14 이상

### 설치 및 실행

1. **저장소 클론**
   ```bash
   git clone <repository-url>
   cd LookFit
   ```

2. **데이터베이스 설정**
   - MySQL 데이터베이스 생성
   - `Lookfit_db.sql` 파일로 스키마 및 데이터 import

3. **환경 변수 설정**
   - `src/main/resources/application.yml` 파일 수정
   - `src/main/resources/application-oauth.properties` 파일 수정 (OAuth2 설정)

4. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

5. **빌드**
   ```bash
   ./gradlew build
   ```

---

## 🔧 개발 환경

### 환경 변수

`application.yml`에서 다음 설정을 확인하세요:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lookfit_db
    username: your_username
    password: your_password
```

### OAuth2 설정

`application-oauth.properties`에서 OAuth2 클라이언트 정보를 설정하세요:

```properties
spring.security.oauth2.client.registration.google.client-id=your_client_id
spring.security.oauth2.client.registration.google.client-secret=your_client_secret
jwt_secret_key=your_jwt_secret_key
```

---

## 📝 개발 현황

### ✅ 완료 (2026-02-01)

#### Backend
- [x] 프로젝트 기본 구조 설정 (DDD 아키텍처)
- [x] 데이터베이스 설계 및 엔티티 작성 (12개 엔티티)
- [x] **OAuth2 소셜 로그인 완성**
  - Google OAuth2 연동
  - JWT 토큰 발급 및 인증
  - 자동 회원 생성 (첫 로그인 시)
  - 한글 이름 UTF-8 인코딩 처리
- [x] **상품 관리 API**
  - 상품 목록 조회 (페이징, 정렬, 필터링)
  - 상품 상세 조회
  - 상품 이미지 URL 지원
- [x] **장바구니 API**
  - 장바구니 조회
  - 상품 추가
  - 수량 변경
  - 상품 삭제 (트랜잭션 처리)
- [x] **주문 API**
  - 주문 생성 (재고 차감)
  - 주문 내역 조회
  - 주문 상세 조회
  - OrderItem 엔티티 (주문 상품 정보)
- [x] **보안 및 예외 처리**
  - SecurityConfig (CORS, JWT 필터)
  - GlobalExceptionHandler
  - ErrorCode 정의
  - REST API 401/403 처리

#### Frontend
- [x] React + Vite 프로젝트 설정
- [x] **디자인 시스템 구축**
  - Design Tokens (색상, 타이포그래피, 간격)
  - Lookpin 스타일 UI 컴포넌트
- [x] **페이지 구현**
  - 홈 (상품 목록)
  - 로그인 (Google OAuth2)
  - 상품 상세
  - 장바구니
  - 주문 내역
- [x] **기능 구현**
  - OAuth2 로그인 플로우
  - JWT 토큰 기반 인증
  - 장바구니 CRUD
  - 상품 이미지 표시
  - Header 환영 메시지 ("환영합니다, {이름}님")

#### Testing
- [x] Puppeteer E2E 테스트
  - 쇼핑 플로우 테스트 (9/9 통과)
  - 상품 상세 테스트 (13/13 통과)
  - 장바구니 플로우 테스트 (4/4 통과)
  - OAuth2 로그인 테스트 스크립트

#### Database
- [x] MySQL UTF-8 인코딩 설정
- [x] 상품 이미지 URL 데이터 추가 (Unsplash)
- [x] Member 테이블 memberid varchar(50) 설정

### 🚧 진행 중

- [ ] 프론트엔드 버튼 색상 이슈 해결 (찜하기, AI 착장샷 버튼)

### 📋 다음 단계 (Phase 4)

- [ ] AI 착장샷 기능 (프로젝트 핵심 차별점)
  - AI 서비스 선정 (Stable Diffusion / Replicate)
  - 착장샷 API 설계 및 구현
  - 비동기 이미지 처리
- [ ] 찜하기 (Wishlist) API 구현
- [ ] 리뷰 & Q&A 기능
- [ ] 검색 기능 강화

---

## 👥 팀

프로젝트 개발자 정보는 별도로 관리됩니다.

---

## 📄 라이선스

이 프로젝트의 라이선스 정보를 명시하세요.

---

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.

---

**Made with ❤️ by LookFit Team**
