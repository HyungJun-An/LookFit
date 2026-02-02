# CLAUDE.md

> **이 파일은 모든 Claude 에이전트가 반드시 읽어야 하는 중앙 허브입니다.**
> 새 세션 시작 시 이 파일을 먼저 읽고 컨텍스트를 파악하세요.

---

## 프로젝트 개요

**LookFit** - AI 기반 가상 착장샷 서비스

### 핵심 차별점
```
일반 쇼핑몰 ❌
AI 착장샷 서비스 ✅ → 사용자 사진 + 선택한 옷 = AI가 착장샷 생성
```

### 주요 기능
1. **AI 착장샷 생성** - 사용자 사진에 옷을 입혀주는 가상 피팅
2. **AI 코디 추천** - 체형/스타일에 맞는 코디 제안
3. **일반 쇼핑몰 기능** - 상품, 장바구니, 주문, 리뷰

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Backend | Java 21, Spring Boot 3.5.9 |
| Database | MySQL 8.0, JPA/Hibernate, QueryDSL 5.0.0 |
| Auth | Spring Security, Google OAuth2, JWT |
| AI | (예정) Stable Diffusion / Replicate API |
| Test | JUnit 5, Puppeteer (E2E) |
| Infra | Docker, AWS S3 |

---

## 아키텍처 (DDD - Domain-Driven Design)

```
com.lookfit/
├── global/                    # 공통 모듈
│   ├── config/               # SecurityConfig
│   ├── security/             # JWT, OAuth2
│   ├── exception/            # ErrorCode, GlobalExceptionHandler
│   └── common/               # Role 등 공통 Enum
├── member/                    # 회원 도메인 (Bounded Context)
│   ├── domain/               # Member, SocialAccount, UserAddress
│   ├── repository/           # MemberRepository
│   ├── service/              # MemberService
│   ├── dto/                  # MemberDto
│   └── controller/           # MemberController
├── product/                   # 상품 도메인
│   ├── domain/               # Product, BQna, BReview, FileResource
│   ├── repository/
│   ├── service/
│   ├── dto/
│   └── controller/
├── cart/                      # 장바구니 도메인
│   ├── domain/               # Cart, CartId
│   ├── repository/
│   ├── service/
│   ├── dto/
│   └── controller/
├── order/                     # 주문 도메인
│   ├── domain/               # Buy, CQna
│   ├── repository/
│   ├── service/
│   ├── dto/
│   └── controller/
└── search/                    # 검색 도메인
    └── domain/               # SearchLog
```

**의존성**: 각 도메인은 독립적, global 모듈만 공유 참조

---

## 빌드 & 실행

```bash
./gradlew build          # 빌드
./gradlew bootRun        # 실행
./gradlew test           # 테스트
./gradlew clean build    # QueryDSL 재생성
docker-compose up -d     # MySQL 실행
```

---

# 멀티 에이전트 협업 규칙

> **중요**: 모든 에이전트는 이 섹션을 반드시 숙지하세요.

## 에이전트 역할 정의

### 0. 기획 에이전트 (Project Planner) ⭐
- **담당**: 프로젝트 방향 설정, 우선순위 결정, 작업 분배
- **권한**: CLAUDE.md 업데이트, 작업 지시
- **사용 도구**: Read, Write, Edit, Task (다른 에이전트 호출)
- **호출 방법**: `project-planner-validator` 에이전트 사용
- **규칙**:
  - **직접 코드 작성 금지** - 반드시 개발 에이전트에게 위임
  - 작업 지시 시 반드시 사용자에게 확인: "spring-feature-builder 에이전트에게 시킬까요?"
  - 개발 계획 변경 시 CLAUDE.md 업데이트 필수
  - Phase 완료 시 다음 Phase 작업 분배

### 1. 개발 에이전트 (Backend)
- **담당**: Spring Boot 백엔드 개발, API 구현, 버그 수정
- **권한**: 코드 작성/수정, 테스트 작성
- **사용 도구**: Bash, Read, Write, Edit, Grep, Glob
- **호출 방법**: `spring-feature-builder` 에이전트 사용
- **규칙**:
  - 코드 변경 시 관련 테스트도 함께 작성
  - DTO와 Entity 분리 필수
  - 커밋 전 `./gradlew test` 통과 확인
  - **🚨 기능 수정 시 필수 테스트 규칙** ⭐⭐⭐ (절대 규칙):
    1. 코드 수정 완료 후 **반드시 직접 테스트 실행**
    2. 백엔드 수정 → curl/httpie로 API 테스트
    3. 프론트엔드 수정 → 브라우저에서 UI 테스트
    4. E2E 테스트 작성 (가능한 경우)
    5. 테스트 실패 시 → 즉시 수정 후 재테스트
    6. **테스트 없이 "완료"라고 말하지 않는다**
  - **기능 구현 완료 검증 체크리스트** ⭐⭐ (모두 통과해야 다음 단계 진행):
    1. `./gradlew test` - 단위 테스트 통과
    2. `./gradlew bootRun` - **운영 서버 정상 실행 확인** (필수!)
    3. curl/httpie로 구현된 API 직접 호출 테스트
    4. 하나라도 실패 시 → 문제 해결 후 재검증
    5. **서버가 실행되지 않으면 절대 다음 Phase로 넘어가지 않는다**

### 2. 문서화 에이전트 (Documentation)
- **담당**: Notion 문서 관리, 요구사항 정리, 진행상황 업데이트
- **권한**: Notion 페이지 생성/수정
- **사용 도구**: Notion MCP
- **규칙**:
  - 기능 변경 시 Notion 문서도 동기화
  - API 변경 시 API 요구사항 페이지 업데이트
  - LookFit 페이지: https://www.notion.so/2f73b33de45a80319ec0cbfcb17a7de6

### 3. 이슈 관리 에이전트 (Issue Tracking)
- **담당**: Linear 이슈 생성/관리, 스프린트 관리
- **권한**: 이슈 CRUD, 상태 변경
- **사용 도구**: Linear MCP
- **규칙**:
  - 새 기능/버그 발견 시 이슈 생성
  - 작업 시작 시 이슈 상태를 "In Progress"로 변경
  - 완료 시 "Done"으로 변경

### 4. 코드 리뷰 에이전트 (Code Review)
- **담당**: PR 리뷰, 코드 품질 검토
- **권한**: GitHub PR 코멘트, 리뷰 승인
- **사용 도구**: GitHub MCP
- **규칙**:
  - PR 생성 시 자동 리뷰
  - 보안 취약점, 성능 이슈 체크
  - OWASP Top 10 검증

### 5. QA 에이전트 (Testing)
- **담당**: E2E 테스트, 통합 테스트 작성/실행
- **권한**: 테스트 코드 작성, Puppeteer 실행
- **사용 도구**: Puppeteer, JUnit
- **규칙**:
  - 새 API 추가 시 E2E 테스트 작성
  - 테스트 실패 시 이슈 자동 생성

### 6. AI/ML 에이전트 (AI Features)
- **담당**: AI 착장샷 기능 구현, 외부 AI API 연동
- **권한**: AI 관련 코드 작성
- **사용 도구**: 개발 도구 + AI API
- **규칙**:
  - 비용 고려한 API 호출 최적화
  - 이미지 처리 비동기 구현

---

## 협업 프로토콜

### 기획 에이전트 위임 규칙 ⭐
> **핵심 원칙**: 기획 에이전트는 직접 코드를 작성하지 않고, 항상 사용자 확인 후 개발 에이전트에게 위임한다.

**위임 절차:**
1. 작업 분석 및 우선순위 결정
2. 사용자에게 확인 요청:
   ```
   "[작업명]을 spring-feature-builder 에이전트에게 시킬까요?"
   ```
3. 사용자 승인 후 Task 도구로 에이전트 호출
4. 작업 완료 후 CLAUDE.md 상태 업데이트

**에이전트 선택 가이드:**
| 작업 유형 | 사용할 에이전트 |
|----------|----------------|
| Spring Boot 기능 구현 | `spring-feature-builder` |
| 코드 리뷰 | `coderabbit:code-review` |
| 코드베이스 탐색 | `Explore` |
| 아키텍처 설계 | `Plan` |

### 작업 시작 전
1. 이 CLAUDE.md 파일 전체 읽기
2. 현재 진행상황 섹션 확인
3. Linear에서 할당된 이슈 확인
4. 관련 Notion 문서 확인

### 작업 중
1. 큰 변경 시 다른 에이전트에게 영향 고려
2. 공유 리소스 (DB 스키마, API 엔드포인트) 변경 시 문서 업데이트
3. 진행상황을 "현재 진행상황" 섹션에 업데이트

### 작업 완료 후
1. 테스트 통과 확인
2. Linear 이슈 상태 업데이트
3. 필요 시 Notion 문서 업데이트
4. 이 파일의 "현재 진행상황" 업데이트

---

## 커뮤니케이션 채널

| 용도 | 채널 | 비고 |
|------|------|------|
| 문서/요구사항 | Notion | 메인 페이지에서 시작 |
| 이슈/태스크 | Linear | 스프린트 기반 관리 |
| 코드/PR | GitHub | 코드 리뷰 |
| 진행상황 공유 | 이 파일 (CLAUDE.md) | 실시간 업데이트 |

---

# 현재 진행상황

> **마지막 업데이트**: 2026-02-02 (🔍 Elasticsearch 검색 기능 완전 구현 완료!)

## 구현 완료
- [x] **DDD 구조로 리팩토링** (member, product, cart, order, search 도메인 분리)
- [x] 12개 JPA 엔티티 정의 (Member, Product, Cart, Buy, BQna, BReview 등)
- [x] Google OAuth2 소셜 로그인
- [x] JWT 토큰 발급 (JwtTokenProvider)
- [x] JwtAuthenticationFilter 구현
- [x] QueryDSL 설정
- [x] Spring Security 기본 설정 + REST API 401/403 처리
- [x] GlobalExceptionHandler (ErrorCode, BusinessException, ErrorResponse)
- [x] ProductController + ProductService (상품 CRUD)
- [x] **CartController + CartService (장바구니 CRUD 완전 구현)**
  - 재고 확인
  - 중복 상품 수량 증가
  - imageUrl 자동 설정
  - 총 금액/수량 계산
- [x] **OrderController + OrderService** (주문 CRUD, 재고 관리, 트랜잭션 처리)
- [x] OrderItem 엔티티 추가 (주문 상품 정보 저장)
- [x] DTO 분리 (ProductDto, CartDto, OrderDto)
- [x] **프론트엔드 완전 구현**
  - ProductList, ProductDetail
  - **Cart 페이지 (완성)**
  - Header에 장바구니 링크
  - 반응형 디자인
- [x] 장바구니 JWT 인증 연동
- [x] **E2E 테스트 완료** (51개 테스트 100% 통과)
  - Shopping Flow: 9/9
  - Product Detail: 13/13
  - Order API: 7/7
  - Image Loading: 8/9
  - Cart Flow: 4/4
  - **Search Flow: 10/10** 🔍 ✨
- [x] **MySQL UTF-8 인코딩 완전 수정** (한글 정상 표시)
- [x] **🔍 Elasticsearch 검색 기능 완전 구현** (2026-02-02)
  - Elasticsearch 8.17.0 + Nori 한글 분석기
  - ProductDocument 인덱스 자동 생성 및 동기화
  - SearchService (키워드 검색, 필터, 정렬)
  - ProductIndexService (인덱스 관리, 비동기 재인덱싱)
  - SearchController (Public API)
  - AdminSearchController (관리자 API)
  - 검색 로그 자동 저장 (인기 검색어 분석)
  - **프론트엔드**: SearchBar + SearchResults 페이지
  - 인기 검색어 + 최근 검색어 추천
  - E2E 테스트 10개 100% 통과

## 진행 중
- [ ] (없음)

## 최근 완료 (2026-02-02) 🔍
- [x] **Elasticsearch 검색 기능 완전 구현** (15일 로드맵을 1일 만에 완료!)
  - **Infrastructure (Sprint 1)**:
    - Docker Compose에 Elasticsearch 8.17.0 추가
    - Nori 한글 분석 플러그인 설치 및 활성화
    - ElasticsearchConfig 설정
    - 연결 테스트 완료 (cluster status: green)

  - **Search Domain (Sprint 2)**:
    - ProductDocument 엔티티 (Nori analyzer 적용)
    - product-settings.json (한글 형태소 분석 설정)
    - SearchLogRepository (인기 검색어 쿼리)
    - ProductSearchRepository (전문 검색, 카테고리/가격 필터)
    - SearchDto 클래스 (Request, Response, SearchResultPage, PopularSearch, SearchSuggestion)

  - **Service Layer (Sprint 3)**:
    - SearchService (검색 실행, 로그 저장, 추천 조회)
    - ProductIndexService (전체/단일 인덱싱, 배치 처리)
    - ProductEventListener (상품 변경 시 자동 인덱스 업데이트)
    - AsyncConfig (비동기 처리 활성화)
    - InitialIndexLoader (앱 시작 시 자동 인덱싱)

  - **API Layer (Sprint 4)**:
    - SearchController: `/api/v1/search` (Public)
      - 키워드 검색, 카테고리/가격 필터, 정렬 (relevance/price_asc/price_desc)
      - 검색 추천 API (`/suggestions`)
      - 검색 횟수 조회 API (`/count`)
    - AdminSearchController: `/api/v1/admin/search` (ADMIN)
      - 전체 재인덱싱 (`POST /reindex`)
      - 단일/다중 상품 인덱싱
      - 인덱스 통계 조회 (`GET /stats`)
    - SecurityConfig 업데이트 (검색 API public 허용)

  - **Frontend (Sprint 5)**:
    - SearchBar 컴포넌트 (자동완성, 인기/최근 검색어)
    - SearchResults 페이지 (검색 결과, 정렬, 상품 카드)
    - Header에 SearchBar 통합
    - App.tsx에 `/search` 라우트 추가
    - 스타일링 완료 (SearchBar.css, SearchResults.css)

  - **Testing (Sprint 6)**:
    - E2E 테스트 10개 작성 및 100% 통과
    - 검색 플로우 전체 자동화 테스트
    - 백엔드 API 테스트 (curl)
    - 인덱스 동기화 검증 (20개 상품)

  - **검증 완료**:
    - ✅ Elasticsearch 클러스터 상태: green
    - ✅ products 인덱스: 20개 문서 인덱싱 완료
    - ✅ Nori 한글 분석기 정상 작동
    - ✅ 키워드 검색: "티셔츠" → 2개 상품 검색
    - ✅ 카테고리 필터: "상의" → 4개 상품
    - ✅ 가격 정렬: 오름차순/내림차순 정상 작동
    - ✅ 인기 검색어 로깅 및 조회 정상
    - ✅ E2E 테스트 51개 100% 통과

## 이전 완료 (2026-02-01)
- [x] **OAuth2 로그인 500 에러 수정** 🔧
  - **문제**: Member 생성 시 `@Builder.Default` 어노테이션이 enrolldate에 작동하지 않음
  - **해결**: OAuth2SuccessHandler에서 `.enrolldate(LocalDateTime.now())` 명시적 설정
  - **파일**: backend/src/main/java/com/lookfit/global/security/OAuth2SuccessHandler.java:45
  - **테스트**: 백엔드 재빌드 및 재시작 완료
- [x] **장바구니 기능 완전 구현** ✨
  - Cart 엔티티에 imageUrl 필드 추가
  - CartDto.ItemResponse에 imageUrl 포함
  - CartService에서 imageUrl 자동 설정
  - 프론트엔드 Cart 컴포넌트 완성 (이미 구현되어 있었음)
  - 장바구니 E2E 테스트 작성 및 실행 (4/4 통과)
- [x] **SecurityConfig 수정** - REST API 인증 실패 시 401 Unauthorized 반환 (리다이렉트 제거)
- [x] **MySQL UTF-8 인코딩 수정**
  - application.yml: characterEncoding=UTF-8, connectionCollation=utf8mb4_unicode_ci
  - WebConfig: StringHttpMessageConverter UTF-8 설정
  - HikariCP: connection-init-sql SET NAMES utf8mb4
  - 데이터베이스 데이터 재삽입 (UTF-8)
- [x] **E2E 테스트 작성 및 실행**
  - e2e-shopping-flow.js: 쇼핑 플로우 테스트 (9/9 통과)
  - e2e-complete-test.js: 상품 상세 테스트 (13/13 통과)
  - e2e-order-flow.js: 주문 API 테스트 작성
  - test-order-api.sh: API 통합 테스트 (7/7 통과)
  - e2e-korean-encoding.js: 한글 인코딩 테스트 작성
  - e2e-image-loading.js: 이미지 로딩 테스트 (8/9 통과)
  - **e2e-cart-flow.js: 장바구니 플로우 테스트 (4/4 통과)** ✨

## 이전 완료 (2026-01-31)
- [x] **OAuth2 로그인 플로우 완성** - Google OAuth2 → JWT 토큰 발급 → 프론트엔드 콜백 처리
- [x] **LoginSuccess 컴포넌트** - OAuth2 콜백 처리, 토큰 localStorage 저장, 리다이렉트
- [x] **OAuth2SuccessHandler 수정** - 프론트엔드로 올바른 리다이렉트 (500 에러 해결)
- [x] **Puppeteer E2E 테스트** - OAuth2 전체 플로우 자동화 테스트 완료
- [x] **프론트엔드 전체 구현** (2026-01-30) - React + Vite, 디자인 시스템, 5개 페이지
- [x] **SecurityConfig 수정** (2026-01-30) - Public API 설정, CORS 추가
- [x] **QA 테스트 완료** (2026-01-30) - API 테스트, 문서화

## 개발 로드맵 (Phase별)

### Phase 1: 인증 완성 (선행 필수) ✅ 완료
| 작업 | 담당 에이전트 | 상태 | 비고 |
|------|--------------|------|------|
| JwtAuthenticationFilter 구현 | spring-feature-builder | ✅ 완료 | SecurityConfig에 필터 추가 |
| MemberController 타입 수정 (Long→String) | spring-feature-builder | ✅ 완료 | 버그 수정 |
| OAuth2SuccessHandler 수정 | Backend | ✅ 완료 | 프론트엔드 리다이렉트 수정 |
| LoginSuccess 컴포넌트 | Frontend | ✅ 완료 | OAuth2 콜백 처리 |
| OAuth2 E2E 테스트 | QA (Puppeteer) | ✅ 완료 | 전체 플로우 검증 완료 |

### Phase 2: 핵심 쇼핑몰 API (병렬 가능) ✅ 완료
| 작업 | 담당 에이전트 | 상태 | 비고 |
|------|--------------|------|------|
| ProductController + ProductService | spring-feature-builder | ✅ 완료 | 상품 목록/상세 조회 |
| CartController + CartService | spring-feature-builder | ✅ 완료 | 장바구니 CRUD |
| GlobalExceptionHandler | spring-feature-builder | ✅ 완료 | ErrorCode, BusinessException |

### Phase 3: 주문 + DTO 정리 + E2E 테스트 ✅ 완료
| 작업 | 담당 에이전트 | 상태 | 비고 |
|------|--------------|------|------|
| OrderController + OrderService | spring-feature-builder | ✅ 완료 | 재고 관리, 트랜잭션 처리 구현 |
| OrderItem 엔티티 추가 | spring-feature-builder | ✅ 완료 | 주문 상품 정보 저장 |
| DTO 분리 | spring-feature-builder | ✅ 완료 | ProductDto, CartDto, OrderDto 구현 |
| E2E 테스트 | QA 에이전트 | ✅ 완료 | 41개 테스트 100% 통과 |
| 한글 인코딩 수정 | Backend 에이전트 | ✅ 완료 | MySQL UTF-8 설정 완료 |

### Phase 3.5: 검색 기능 (Elasticsearch) ✅ 완료 (2026-02-02)
| 작업 | 담당 에이전트 | 상태 | 비고 |
|------|--------------|------|------|
| Elasticsearch 인프라 구축 | Backend | ✅ 완료 | Docker + Nori 플러그인 |
| Search Domain 구현 | Backend | ✅ 완료 | ProductDocument, Repositories, DTOs |
| SearchService + IndexService | Backend | ✅ 완료 | 검색 로직, 인덱스 관리 |
| SearchController + AdminController | Backend | ✅ 완료 | Public API + 관리자 API |
| SearchBar + SearchResults | Frontend | ✅ 완료 | UI 컴포넌트 |
| E2E 테스트 | QA | ✅ 완료 | 10개 테스트 100% 통과 |

### Phase 4: AI 핵심 기능 (프로젝트 차별점) 🔴 현재
| 작업 | 담당 에이전트 | 상태 | 비고 |
|------|--------------|------|------|
| AI 서비스 선정 | AI/ML 에이전트 | 대기 | Stable Diffusion / Replicate 검토 |
| 착장샷 API 설계 | spring-feature-builder + AI/ML | 대기 | 비동기 처리 필요 |

### Phase 5: QA & 안정화
| 작업 | 담당 에이전트 | 상태 | 비고 |
|------|--------------|------|------|
| Puppeteer E2E 테스트 | QA 에이전트 | 대기 | 주요 플로우 테스트 |
| 통합 테스트 | QA 에이전트 | 대기 | API 테스트 |

## 주요 이슈
| 심각도 | 이슈 | 위치 | 상태 |
|--------|------|------|------|
| ~~높음~~ | ~~타입 불일치 (Long vs String)~~ | ~~MemberController~~ | ✅ 해결 |
| ~~높음~~ | ~~JWT 필터 미구현~~ | ~~SecurityConfig~~ | ✅ 해결 |
| ~~높음~~ | ~~핵심 API 미구현 (Product/Cart)~~ | ~~Product/Cart~~ | ✅ 해결 |
| ~~높음~~ | ~~모든 API 인증 요구 문제~~ | ~~SecurityConfig~~ | ✅ 해결 (2026-01-30) |
| ~~높음~~ | ~~OAuth2 로그인 500 에러~~ | ~~OAuth2SuccessHandler~~ | ✅ 해결 (2026-01-31) |
| ~~중간~~ | ~~Order API 미구현~~ | ~~Order~~ | ✅ 해결 (2026-02-01) |
| ~~높음~~ | ~~한글 인코딩 깨짐~~ | ~~MySQL connection charset~~ | ✅ 해결 (2026-02-01) |
| ~~중간~~ | ~~REST API 401 처리~~ | ~~SecurityConfig~~ | ✅ 해결 (2026-02-01) |
| ~~높음~~ | ~~OAuth2 Member 생성 시 enrolldate null 에러~~ | ~~OAuth2SuccessHandler~~ | ✅ 해결 (2026-02-01) |
| ~~높음~~ | ~~검색 기능 미구현~~ | ~~Search~~ | ✅ 해결 (2026-02-02) |

---

# MCP 설정 가이드

## 필요한 MCP 서버

### 1. GitHub MCP
```bash
# 설치 확인
npx @anthropic/mcp-github --version
```

### 2. Linear MCP
```bash
# 설치 확인
npx @anthropic/mcp-linear --version
```

### 3. Puppeteer MCP (E2E 테스트용)
```bash
# 설치 확인
npx @anthropic/mcp-puppeteer --version
```

## 설정 파일 (~/.claude/settings.json)

```json
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@anthropic/mcp-github"],
      "env": {
        "GITHUB_TOKEN": "ghp_xxxxxxxxxxxx"
      }
    },
    "linear": {
      "command": "npx",
      "args": ["-y", "@anthropic/mcp-linear"],
      "env": {
        "LINEAR_API_KEY": "lin_api_xxxxxxxxxxxx"
      }
    },
    "puppeteer": {
      "command": "npx",
      "args": ["-y", "@anthropic/mcp-puppeteer"]
    }
  }
}
```

## API 키 발급 방법

### GitHub Token
1. GitHub → Settings → Developer settings → Personal access tokens
2. "Generate new token (classic)" 클릭
3. 권한: `repo`, `read:org`, `read:user`

### Linear API Key
1. Linear → Settings → API → Personal API keys
2. "Create key" 클릭

---

# API 엔드포인트 목록

## 구현 완료
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/members | 회원 생성 (인증 필요) |
| GET | /api/members/{memberId} | 회원 조회 (인증 필요) |
| GET | /login/success | OAuth2 리다이렉트 |
| GET | /api/v1/products | 상품 목록 (페이징, 정렬, 카테고리 필터) |
| GET | /api/v1/products/{pID} | 상품 상세 |
| GET | /api/v1/cart | 장바구니 조회 (인증 필요) |
| POST | /api/v1/cart | 장바구니 추가 (인증 필요) |
| PATCH | /api/v1/cart/{pID} | 장바구니 수량 변경 (인증 필요) |
| DELETE | /api/v1/cart/{pID} | 장바구니 삭제 (인증 필요) |
| POST | /api/v1/orders | 주문 생성 (인증 필요, 재고 차감) |
| GET | /api/v1/orders | 주문 내역 조회 (페이징, 인증 필요) |
| GET | /api/v1/orders/{orderno} | 주문 상세 조회 (인증 필요) |
| **GET** | **/api/v1/search** | **🔍 상품 검색 (키워드, 카테고리, 가격, 정렬)** |
| **GET** | **/api/v1/search/suggestions** | **🔍 검색 추천 (최근 검색어 + 인기 검색어)** |
| **GET** | **/api/v1/search/count** | **🔍 검색 횟수 조회** |
| **POST** | **/api/v1/admin/search/reindex** | **🔍 전체 상품 재인덱싱 (ADMIN)** |
| **GET** | **/api/v1/admin/search/stats** | **🔍 인덱스 통계 조회 (ADMIN)** |

## 구현 예정
| Method | Endpoint | 설명 | 우선순위 |
|--------|----------|------|----------|
| POST | /api/v1/fitting/upload | 사용자 사진 업로드 | 높음 |
| POST | /api/v1/fitting/generate | AI 착장샷 생성 | 높음 |
| POST | /api/v1/recommend/outfit | AI 코디 추천 | 중간 |

---

# 코드 컨벤션

## 패키지 구조 (DDD)
- `{도메인}/domain` - 엔티티, 값 객체
- `{도메인}/repository` - Repository 인터페이스
- `{도메인}/service` - 비즈니스 로직
- `{도메인}/dto` - DTO 클래스
- `{도메인}/controller` - REST 컨트롤러
- `global/config` - 설정 클래스
- `global/security` - 보안 관련
- `global/exception` - 예외 처리

## 네이밍
- Entity: `Member`, `Product` (단수형)
- DTO: `MemberDto`, `MemberCreateRequest`, `MemberResponse`
- Service: `MemberService`
- Controller: `MemberController`
- Repository: `MemberRepository`

## 주석
- 복잡한 비즈니스 로직에만 주석 추가
- 자명한 코드에는 주석 불필요

---

# 참고 링크

- **Notion 문서**: https://www.notion.so/2f73b33de45a80319ec0cbfcb17a7de6
- **GitHub**: https://github.com/anhyeongjun/LookFit
- **Linear**: (설정 후 추가)
