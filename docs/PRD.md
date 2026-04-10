# LookFit — Product Requirements Document (PRD)

| 항목 | 값 |
|-----|----|
| **문서 버전** | 1.0 |
| **작성일** | 2026-04-10 |
| **작성자** | Claude Code (CLAUDE.md 및 코드베이스 기반 추출) |
| **상태** | Draft — 사용자 검토 필요 |
| **기반 문서** | `CLAUDE.md`, `README.md`, `docs/AI_FITTING_PLAN_HUGGINGFACE.md`, `NAMING_CONVENTION.md`, 코드베이스 |

> ⚠️ 이 문서는 **기존 기획 문서가 아닌, 구현된 코드와 CLAUDE.md로부터 역추출한 PRD**다. 원래 기획 의도와 차이가 있을 수 있으며, "Open Questions" 섹션의 항목은 사용자 확인이 필요하다.

---

## 1. Executive Summary

**LookFit**은 "사고 나서 후회"를 줄이기 위해, 사용자의 사진에 상품을 AI로 입혀서 **구매 전에 실제 착용 이미지를 보여주는** 모바일 우선(Mobile-First) 커머스 플랫폼이다.

일반 쇼핑몰이 상품 사진 + 모델 컷에 의존하는 데 반해, LookFit은 **사용자 본인의 사진 + 선택한 상품 → AI 생성 착장샷**이라는 단일 상호작용으로 구매 전 체험 격차를 메운다.

- **차별점**: AI 가상 피팅 (Hugging Face IDM-VTON)
- **보조 기능**: 일반 쇼핑몰 풀스택 (카탈로그·검색·장바구니·찜·주문·리뷰)
- **시장**: 한국어 사용자 (Elasticsearch Nori 한글 형태소 분석 채택)
- **현재 상태**: MVP 기능 구현 완료, 프로덕션 배포 준비 단계 (EC2 + Docker + Vercel)

---

## 2. Vision & Goals

### 2.1 Vision
> "옷을 직접 입어보지 않아도, 내 몸에 어울리는지 알 수 있게 한다."

### 2.2 비즈니스 목표
1. **반품률 감소** — 상품에 대한 구매 전 확신도를 높여 반품 비용을 낮춘다.
2. **전환율 향상** — 장바구니 이탈을 줄이고, "고민 중" 상태에서 구매로의 전환을 유도한다.
3. **차별적 사용자 경험** — 일반 쇼핑몰과 구분되는 기술 기반 경험으로 리텐션 확보.

### 2.3 제품 목표 (Product Goals)
- **P0**: AI 가상 피팅을 부드럽게 사용할 수 있는 전체 플로우 제공
- **P0**: 피팅을 실행하기 위한 상품·회원·인증 최소 골격
- **P1**: 구매 신뢰를 높이는 리뷰·별점
- **P1**: 한국어 상품 검색 품질 확보 (Nori)
- **P2**: 찜·카트·주문 (풀 커머스 기본기)
- **P3**: 운영 가능한 수준의 관측성·배포 자동화

---

## 3. Target Users (Personas)

### 3.1 Primary Persona — "혼쇼족 민지" (25세, 여성, 디지털 네이티브)
- 온라인 쇼핑 월 5회 이상, 반품 경험 있음
- 몸에 맞을지 확신이 안 서서 장바구니에서 이탈한 경험이 잦음
- 모바일(스마트폰)에서 주로 쇼핑
- Google 계정 기반 로그인을 선호, 회원가입 양식 기피
- **Needs**: "내 몸에 입혀보고 싶다", "다른 사람 후기도 보고 싶다"

### 3.2 Secondary Persona — "가성비 수민" (30세, 남성)
- 후기를 꼼꼼히 읽고 구매 결정
- 평점 낮은 상품은 피하고, 이미지 있는 리뷰를 신뢰
- **Needs**: "실제 구매자가 올린 리뷰 이미지", "평균 별점과 리뷰 수 요약"

### 3.3 Out-of-scope Personas (현재 버전)
- 쇼핑몰 운영자·관리자 (관리자 대시보드 미구현, 일부 admin API만 존재)
- B2B 입점 판매자

---

## 4. Core Value Proposition & Scope

### 4.1 고유 가치 (Moat)
```
일반 쇼핑몰:  상품 사진 + 모델 컷 → "모델에게는 어울리는데 나한테는?"
LookFit:     상품 사진 + 내 사진 → "나에게 어울리는지 바로 확인"
```

### 4.2 In Scope (현재 버전)
- ✅ Google OAuth2 기반 소셜 로그인
- ✅ 상품 카탈로그 (목록·상세·이미지)
- ✅ Elasticsearch + Nori 한국어 검색
- ✅ 장바구니 (재고 확인, 수량 변경)
- ✅ 찜 목록
- ✅ 주문 (트랜잭션 기반 재고 차감)
- ✅ 리뷰 및 별점 (구매 확인, 이미지 업로드)
- ✅ **AI 가상 피팅 (Hugging Face IDM-VTON)** ← 핵심 차별점
- ✅ 모바일 반응형 UI

### 4.3 Out of Scope (현재 미구현, 추후 검토)
- ❌ **결제 연동** — 주문 엔티티는 있지만 PG사 연동 없음
- ❌ **배송 추적·관리** — 주문 이후 물류 플로우 없음
- ❌ **관리자 대시보드** — `AdminSearchController` 외 관리 기능 없음
- ❌ **AI 코디 추천** — `POST /api/v1/recommend/outfit`로 계획만 남아 있음
- ❌ **다국어 지원** — 한국어만 지원
- ❌ **다중 소셜 로그인** — Google 단일 공급자
- ❌ **카카오·네이버·페이스북 공유**
- ❌ **푸시 알림** (신상품, 가격 인하 등)
- ❌ **B2B 판매자 온보딩**
- ❌ **쿠폰·포인트·할인 코드 시스템**

---

## 5. 현재 구현 상태 (Build Status)

### 5.1 도메인별 구현 현황

| 도메인 | 구현 상태 | 주요 산출물 |
|-------|---------|-----------|
| `global` | ✅ Complete | `SecurityConfig`, `JwtAuthenticationFilter`, `OAuth2SuccessHandler`, `GlobalExceptionHandler`, `ErrorCode`, `BusinessException` |
| `member` | ✅ Complete | Google OAuth2 → JWT 발급 → localStorage 저장 전체 플로우 |
| `product` | ✅ Complete | 상품 CRUD, 목록/상세, 이미지 |
| `cart` | ✅ Complete | 장바구니 CRUD, 재고 확인, 수량 머지, imageUrl 스냅샷 |
| `order` | ✅ Complete | 트랜잭션 재고 차감, 주문 내역/상세, `OrderItem` |
| `wishlist` | ✅ Complete | 복합키 기반 찜 추가·해제 |
| `search` | ✅ Complete | ES 인덱싱, Nori 분석, 검색 API, 관리자 재인덱스, 검색 로그, 인기/최근 검색어 |
| `fitting` | ✅ Complete | Hugging Face Python 브릿지, 상태 폴링, 히스토리, GPU quota 처리 |
| 프론트엔드 | ✅ Complete | 전체 페이지 + 모바일 반응형 + E2E 테스트 |
| 배포 | 🟡 **진행 중** | Vercel(FE) + EC2(BE) + Docker + GitHub Actions (현재 브랜치 작업 중) |
| 결제 | ❌ Missing | PG 연동 없음 |
| 관리자 대시보드 | ❌ Missing | 일부 admin API만 존재 |

### 5.2 현재 브랜치 작업 내역 (git status 기준, 2026-04-10)
**main 브랜치에서 진행 중인 수정 사항** (아직 커밋 안 됨):
- `README.md`, `backend/build.gradle.kts`, `OAuth2SuccessHandler.java`, `SearchLogRepository.java`, `application.yml`, `frontend/src/api/axiosInstance.ts`

**untracked (신규)**:
- `.env.production`, `.github/`, `Dockerfile`, `application-prod.yml`, `docker-compose.prod.yml`, `scripts/setup-ec2.sh`, `scripts/setup-vercel-env.sh`
- 운영 문서: `DEPLOYMENT_GUIDE.md`, `ENV_MANAGEMENT.md`, `GOOGLE_OAUTH_FIX.md`, `GOOGLE_OAUTH_SETUP.md`, `TROUBLESHOOTING.md`, `VERCEL_*.md`

**최근 커밋 (최신 5개)**:
- `fix: ReviewForm Authorization 헤더 누락 문제 해결`
- `fix: SearchController 인증 타입 통일`
- `fix: ReviewController 인증 타입 불일치 해결`
- `debug: JWT 인증 디버깅 로그 추가`
- `fix: 리뷰 작성 API 인증 문제 해결`

**해석**: 프로덕션 배포 준비(Phase 6) + JWT 인증 컨트롤러 간 일관성 정리(Phase 5 QA)가 **동시에 진행 중**이다.

---

## 6. 기능 요구사항 (Functional Requirements)

> 형식: `FR-{도메인약자}-{순번}: 요구사항 설명`
> 상태: ✅ 구현 완료 / 🟡 부분 구현 / ❌ 미구현

### FR-AUTH: 인증 및 인가

| ID | 요구사항 | 상태 | 근거 |
|----|--------|------|-----|
| FR-AUTH-001 | Google OAuth2로 소셜 로그인할 수 있어야 한다 | ✅ | `OAuth2SuccessHandler.java` |
| FR-AUTH-002 | 로그인 성공 시 JWT 토큰이 발급되어야 한다 | ✅ | `JwtTokenProvider` |
| FR-AUTH-003 | 토큰은 프론트엔드 `localStorage`에 저장되며, Axios 인터셉터가 모든 요청에 `Authorization: Bearer` 헤더를 자동 부착해야 한다 | ✅ | `axiosInstance.ts` |
| FR-AUTH-004 | 토큰 만료 또는 401 응답 시 자동 로그아웃 및 로그인 페이지로 리다이렉트 | ✅ | `axiosInstance.ts` response interceptor |
| FR-AUTH-005 | 백엔드는 `JwtAuthenticationFilter`로 모든 보호 API의 토큰을 검증한다 | ✅ | `JwtAuthenticationFilter` |
| FR-AUTH-006 | 인증 실패 시 REST API는 401 JSON 응답을 반환해야 한다 (리다이렉트 금지) | ✅ | `SecurityConfig` |
| FR-AUTH-007 | OAuth2 콜백에서 `Member` 생성 시 `enrolldate`를 명시적으로 설정한다 (`@Builder.Default` 사고 방지) | ✅ | `OAuth2SuccessHandler.java:45` |
| FR-AUTH-008 | 다중 소셜 로그인 공급자 지원 (Kakao, Naver) | ❌ | 추후 검토 |
| FR-AUTH-009 | 로그아웃 기능 | 🟡 | 클라이언트 단 `localStorage.clear()`만 있음, 서버 세션 무효화 없음 |

### FR-MEMBER: 회원

| ID | 요구사항 | 상태 |
|----|--------|------|
| FR-MEMBER-001 | 회원은 Google OAuth2를 통해 자동 가입된다 (별도 회원가입 폼 없음) | ✅ |
| FR-MEMBER-002 | `member_id`는 시스템 발급 식별자다 (사용자 입력 아님) | ✅ |
| FR-MEMBER-003 | 동일 Google 계정 재로그인 시 기존 회원으로 식별된다 | ✅ |
| FR-MEMBER-004 | 탈퇴한 회원(`deletedAt IS NOT NULL`)은 로그인할 수 없다 | ✅ (`DELETED_MEMBER` ErrorCode) |
| FR-MEMBER-005 | 회원 탈퇴 API | ❌ |
| FR-MEMBER-006 | 회원 정보 수정 (프로필 이미지, 이름 등) | ❌ |

### FR-PRODUCT: 상품

| ID | 요구사항 | 상태 |
|----|--------|------|
| FR-PRODUCT-001 | 상품 목록을 페이징과 정렬(최신/가격)로 조회할 수 있어야 한다 | ✅ |
| FR-PRODUCT-002 | 카테고리별로 필터링할 수 있어야 한다 | ✅ |
| FR-PRODUCT-003 | 상품 상세 정보(설명, 이미지, 가격, 재고)를 조회할 수 있어야 한다 | ✅ |
| FR-PRODUCT-004 | 상품 이미지가 깨지지 않고 표시되어야 한다 (`getImageUrl()` 유틸 사용) | ✅ |
| FR-PRODUCT-005 | 상품 재고가 0이면 "품절" 상태로 표시된다 | ✅ |
| FR-PRODUCT-006 | 상품 등록/수정/삭제 (관리자 API) | 🟡 부분 구현 |
| FR-PRODUCT-007 | 상품 이미지 다중 업로드 | ❌ |

### FR-SEARCH: 검색

| ID | 요구사항 | 상태 |
|----|--------|------|
| FR-SEARCH-001 | 한국어 키워드로 상품을 검색할 수 있어야 한다 (Nori 형태소 분석) | ✅ |
| FR-SEARCH-002 | 카테고리, 최소·최대 가격으로 필터링할 수 있다 | ✅ |
| FR-SEARCH-003 | 관련도 / 가격 오름차순 / 가격 내림차순으로 정렬 가능 | ✅ |
| FR-SEARCH-004 | 검색 결과는 페이징된다 | ✅ |
| FR-SEARCH-005 | 인기 검색어와 최근 검색어를 추천으로 제공한다 | ✅ |
| FR-SEARCH-006 | 모든 검색은 `SearchLog`(MySQL)에 저장된다 | ✅ |
| FR-SEARCH-007 | 관리자가 전체 상품을 재인덱싱할 수 있다 (`POST /api/v1/admin/search/reindex`) | ✅ |
| FR-SEARCH-008 | 상품이 생성/수정/삭제되면 ES 인덱스가 비동기로 자동 갱신된다 | ✅ (`ProductEventListener`) |
| FR-SEARCH-009 | 자동완성 / 오타 교정 | ❌ |
| FR-SEARCH-010 | 개인화 검색 (유저의 선호 카테고리 반영) | ❌ |

### FR-CART: 장바구니

| ID | 요구사항 | 상태 |
|----|--------|------|
| FR-CART-001 | 로그인 사용자는 상품을 장바구니에 담을 수 있어야 한다 | ✅ |
| FR-CART-002 | 장바구니는 재고 부족 시 추가를 거부한다 (`PRODUCT_OUT_OF_STOCK`) | ✅ |
| FR-CART-003 | 동일 상품 재추가 시 수량이 증가한다 (새 row 생성 금지) | ✅ |
| FR-CART-004 | 수량을 변경할 수 있어야 한다 (`PATCH /api/v1/cart/{pID}`) | ✅ |
| FR-CART-005 | 개별 상품을 삭제할 수 있어야 한다 | ✅ |
| FR-CART-006 | 장바구니 조회 시 총 금액과 총 수량이 계산되어 반환된다 | ✅ |
| FR-CART-007 | 장바구니에는 상품 이미지 URL이 포함된다 (snapshot) | ✅ |
| FR-CART-008 | 전체 비우기 기능 | ❌ |
| FR-CART-009 | 비로그인 게스트 장바구니 (localStorage) | ❌ |

### FR-WISHLIST: 찜 목록

| ID | 요구사항 | 상태 |
|----|--------|------|
| FR-WISHLIST-001 | 로그인 사용자는 상품을 찜할 수 있어야 한다 | ✅ |
| FR-WISHLIST-002 | 찜 목록은 복합키 (`member_id`, `product_id`)로 관리된다 | ✅ |
| FR-WISHLIST-003 | 찜 해제 가능 (`DELETE /api/v1/wishlist/{productId}`) | ✅ |
| FR-WISHLIST-004 | 찜 목록 페이지에서 전체 조회 가능 | ✅ |

### FR-ORDER: 주문

| ID | 요구사항 | 상태 |
|----|--------|------|
| FR-ORDER-001 | 주문 생성 시 단일 트랜잭션으로 재고 차감 + 주문 저장 + 장바구니 비움 처리 | ✅ |
| FR-ORDER-002 | 재고 부족 시 전체 주문이 롤백된다 (부분 성공 금지) | ✅ |
| FR-ORDER-003 | 주문 내역 페이징 조회 가능 | ✅ |
| FR-ORDER-004 | 주문 상세 조회 가능 (타인 주문 접근 불가, `ACCESS_DENIED`) | ✅ |
| FR-ORDER-005 | 주문 상품 정보는 `OrderItem` 엔티티에 저장된다 (주문 당시 스냅샷) | ✅ |
| FR-ORDER-006 | **결제 연동** (PG사 API) | ❌ |
| FR-ORDER-007 | 주문 취소 기능 | ❌ |
| FR-ORDER-008 | 배송 상태 추적 | ❌ |
| FR-ORDER-009 | 영수증·세금계산서 발급 | ❌ |

### FR-REVIEW: 리뷰 및 별점

| ID | 요구사항 | 상태 |
|----|--------|------|
| FR-REVIEW-001 | 리뷰 작성은 **구매자만** 가능해야 한다 (`REVIEW_NOT_PURCHASED`) | ✅ |
| FR-REVIEW-002 | 한 사용자는 한 상품에 **1회만** 리뷰 작성 가능 (`REVIEW_ALREADY_EXISTS`) | ✅ |
| FR-REVIEW-003 | 별점은 1~5 정수로 입력한다 | ✅ |
| FR-REVIEW-004 | 리뷰 내용은 최대 500자, XSS 방지 처리된다 | ✅ |
| FR-REVIEW-005 | 리뷰에 이미지 1장을 업로드할 수 있다 (최대 5MB, `jpg/jpeg/png/webp`) | ✅ |
| FR-REVIEW-006 | 리뷰는 소프트 삭제된다 (`deletedAt` 타임스탬프) | ✅ |
| FR-REVIEW-007 | 본인 리뷰만 수정·삭제 가능 | ✅ |
| FR-REVIEW-008 | 리뷰 목록은 페이징 조회 가능하며 공개 API다 | ✅ |
| FR-REVIEW-009 | 상품별 평균 별점과 리뷰 수 요약 API 제공 | ✅ |
| FR-REVIEW-010 | 작성자 식별자는 마스킹되어 표시된다 (`user***`) | ✅ |
| FR-REVIEW-011 | 본인이 작성한 리뷰는 `isOwner: true`로 표시된다 | ✅ |
| FR-REVIEW-012 | 리뷰 추천/비추천 | ❌ |
| FR-REVIEW-013 | 관리자 리뷰 신고 처리 | ❌ |

### FR-FITTING: AI 가상 피팅 🌟 (핵심 차별 기능)

| ID | 요구사항 | 상태 |
|----|--------|------|
| FR-FITTING-001 | 로그인 사용자는 자신의 사진을 업로드할 수 있다 | ✅ |
| FR-FITTING-002 | 업로드 이미지는 서버 로컬 FS에 저장된다 (`fitting.upload-dir`) | ✅ |
| FR-FITTING-003 | 프론트엔드는 AVIF 이미지를 자동으로 JPEG로 변환한다 (Python 호환성) | ✅ |
| FR-FITTING-004 | 사용자는 의류 카테고리를 선택한다 (`upper_body` / `lower_body` / `dresses`) | ✅ |
| FR-FITTING-005 | AI 피팅 생성 요청 시 즉시 `PROCESSING` 상태가 반환되고, 백엔드가 Python 프로세스를 통해 Hugging Face IDM-VTON 모델을 호출한다 | ✅ |
| FR-FITTING-006 | 피팅 상태를 폴링으로 조회할 수 있다 (`PROCESSING` → `COMPLETED` / `FAILED`) | ✅ |
| FR-FITTING-007 | 완료된 피팅 결과 이미지를 사용자에게 표시한다 | ✅ |
| FR-FITTING-008 | Hugging Face GPU 할당량 초과 시 `GPU_QUOTA_EXCEEDED` ErrorCode로 사용자에게 안내 | ✅ |
| FR-FITTING-009 | 사용자는 자신의 피팅 히스토리를 페이징 조회할 수 있다 | ✅ |
| FR-FITTING-010 | 피팅 결과 이미지는 로컬 FS에 저장된다 (`fitting.result-dir`) | ✅ |
| FR-FITTING-011 | 피팅 이미지 **S3 영속 저장** | ❌ |
| FR-FITTING-012 | 피팅 결과 공유 (SNS, 링크) | ❌ |
| FR-FITTING-013 | 여러 상품 동시 피팅 (상의 + 하의 조합) | ❌ |
| FR-FITTING-014 | 피팅 히스토리에서 공개 여부 토글 | ❌ |
| FR-FITTING-015 | 피팅 결과를 리뷰에 첨부 | ❌ |

### FR-UI: 사용자 인터페이스

| ID | 요구사항 | 상태 |
|----|--------|------|
| FR-UI-001 | 모바일(≤768px), 태블릿(≤1024px), 데스크톱 반응형 지원 | ✅ |
| FR-UI-002 | 모바일에서 햄버거 메뉴로 네비게이션 | ✅ |
| FR-UI-003 | 모든 터치 가능한 요소는 최소 44×44px | ✅ |
| FR-UI-004 | iOS 줌 방지 (input 최소 16px) | ✅ |
| FR-UI-005 | 가로 스크롤 발생 금지 | ✅ |
| FR-UI-006 | `prefers-reduced-motion` 미디어 쿼리 지원 | ✅ |
| FR-UI-007 | 다크 모드 | ❌ |
| FR-UI-008 | 다국어 (i18n) | ❌ |

---

## 7. 비기능 요구사항 (Non-Functional Requirements)

### NFR-PERF: 성능

| ID | 요구사항 | 현재 상태 |
|----|--------|----------|
| NFR-PERF-001 | 상품 목록 API 응답 시간 < 500ms (p95) | 🟡 미측정 |
| NFR-PERF-002 | 검색 API 응답 시간 < 300ms (p95) | 🟡 미측정 |
| NFR-PERF-003 | AI 피팅 생성 응답 시간 < 60초 (Hugging Face 모델 한정) | 🟡 측정 필요 |
| NFR-PERF-004 | N+1 쿼리 방지 (`@EntityGraph`, QueryDSL fetchJoin) | 🟡 부분 적용 |
| NFR-PERF-005 | ES 인덱스 전체 재구축 시간 < 5분 (상품 1만 개 기준) | 🟡 미측정 |
| NFR-PERF-006 | Redis 기반 캐싱 (상품·검색 결과) | ❌ |
| NFR-PERF-007 | CDN을 통한 이미지 배포 | ❌ |

### NFR-SEC: 보안

| ID | 요구사항 | 현재 상태 |
|----|--------|----------|
| NFR-SEC-001 | 모든 비밀키·환경변수는 `.env`로 분리, 저장소에 커밋 금지 | ✅ |
| NFR-SEC-002 | JWT 서명 키는 운영·로컬 환경별 분리 | ✅ |
| NFR-SEC-003 | CORS는 허용 도메인 화이트리스트만 | ✅ |
| NFR-SEC-004 | 리뷰 내용 XSS 방지 (HTML 이스케이프) | ✅ |
| NFR-SEC-005 | 파일 업로드 검증 (확장자, Content-Type, 파일 크기) | ✅ |
| NFR-SEC-006 | SQL Injection 방지 (JPA/QueryDSL 파라미터 바인딩) | ✅ |
| NFR-SEC-007 | 사용자 사진의 개인정보 보호 (삭제 요청 시 실제 파일 제거) | 🟡 Soft delete만 있음 |
| NFR-SEC-008 | JWT를 `localStorage` 대신 `HttpOnly Cookie`로 저장 (XSS 시 탈취 방지) | ❌ 아키텍처 재검토 필요 |
| NFR-SEC-009 | HTTPS 강제 (운영) | 🟡 배포 시 필수 적용 |
| NFR-SEC-010 | Rate Limiting (로그인·리뷰·피팅 생성 API) | ❌ |
| NFR-SEC-011 | GDPR / 개인정보보호법 대응 (사용자 데이터 다운로드·삭제 요청) | ❌ |

### NFR-SCALE: 확장성

| ID | 요구사항 | 현재 상태 |
|----|--------|----------|
| NFR-SCALE-001 | 업로드 이미지 **S3 마이그레이션** (로컬 FS는 수평 확장 불가) | ❌ **Critical Debt** |
| NFR-SCALE-002 | MySQL 읽기 복제본 | ❌ |
| NFR-SCALE-003 | 피팅 작업 큐 분리 (Redis/Kafka 기반) | ❌ |
| NFR-SCALE-004 | Elasticsearch 클러스터화 (현재 단일 노드) | ❌ |

### NFR-AVAIL: 가용성

| ID | 요구사항 | 현재 상태 |
|----|--------|----------|
| NFR-AVAIL-001 | 백엔드는 Elasticsearch가 없어도 **기동 가능**해야 한다 | ❌ `InitialIndexLoader`가 ES 연결 실패 시 앱 기동 차단 |
| NFR-AVAIL-002 | Hugging Face GPU 할당량 초과 시 graceful degradation (안내 메시지) | ✅ |
| NFR-AVAIL-003 | 배포 시 다운타임 최소화 (blue-green 또는 rolling) | ❌ |
| NFR-AVAIL-004 | Actuator `/actuator/health` 헬스체크 | ✅ |
| NFR-AVAIL-005 | Prometheus 메트릭 노출 | ✅ |
| NFR-AVAIL-006 | Grafana 대시보드 | ❌ |

### NFR-A11Y: 접근성

| ID | 요구사항 | 현재 상태 |
|----|--------|----------|
| NFR-A11Y-001 | 모든 이미지에 `alt` 속성 | 🟡 부분 적용 |
| NFR-A11Y-002 | 키보드 네비게이션 가능 | 🟡 부분 적용 |
| NFR-A11Y-003 | `focus-visible` 스타일 적용 | ✅ |
| NFR-A11Y-004 | 색상 대비 WCAG AA 이상 | 🟡 미검증 |
| NFR-A11Y-005 | 스크린리더 호환 (`aria-*`) | ❌ |

### NFR-OPS: 운영

| ID | 요구사항 | 현재 상태 |
|----|--------|----------|
| NFR-OPS-001 | GitHub Actions 기반 CI/CD 파이프라인 | 🟡 진행 중 (`.github/` 신규) |
| NFR-OPS-002 | 도커 이미지 멀티스테이지 빌드 | 🟡 진행 중 |
| NFR-OPS-003 | 환경별 설정 분리 (`application.yml` vs `application-prod.yml`) | ✅ |
| NFR-OPS-004 | 로그 구조화 (JSON) 및 중앙화 | ❌ |
| NFR-OPS-005 | 알림 (Slack, 이메일) | ❌ |

---

## 8. 선정된 User Stories

### US-001: 구매 전 피팅 체험 (P0 — 핵심)
> **As a** 온라인 쇼핑 사용자,
> **I want to** 내 사진에 상품을 입혀서 미리 볼 수 있기를,
> **So that** 옷이 나에게 어울리는지 확신하고 구매할 수 있다.

**Acceptance Criteria**:
- [ ] 로그인한 상태에서 상품 상세 페이지에 "AI 피팅" 버튼이 있다
- [ ] 버튼을 누르면 사진 업로드 UI가 나온다
- [ ] 사진을 업로드하고 "생성" 버튼을 누르면 진행 상태가 표시된다
- [ ] 60초 이내에 결과가 나오거나, 실패 시 명확한 안내가 뜬다
- [ ] 결과 이미지는 저장되어 이후 히스토리에서 다시 볼 수 있다

### US-002: 구매자 리뷰 확인 (P1)
> **As a** 구매를 고민 중인 사용자,
> **I want to** 실제 구매자의 리뷰와 별점을 볼 수 있기를,
> **So that** 상품 품질에 대한 확신을 얻는다.

**Acceptance Criteria**:
- [ ] 상품 상세 페이지 하단에 리뷰 목록이 있다
- [ ] 상품 상단에 평균 별점과 총 리뷰 수가 표시된다
- [ ] 리뷰에 이미지가 있으면 함께 표시된다
- [ ] 로그인하지 않아도 리뷰를 읽을 수 있다

### US-003: 한국어 상품 검색 (P1)
> **As a** 한국어 사용자,
> **I want to** "하얀색 반팔 티셔츠" 같은 자연어로 검색할 수 있기를,
> **So that** 원하는 상품을 빠르게 찾을 수 있다.

**Acceptance Criteria**:
- [ ] 헤더 검색창에 한국어 키워드를 입력하면 관련 상품이 나온다
- [ ] Nori 형태소 분석 덕분에 "티셔츠", "티", "tshirt" 같은 유사어도 검색된다
- [ ] 가격·카테고리 필터로 결과를 좁힐 수 있다
- [ ] 검색 결과가 관련도/가격순으로 정렬 가능하다

### US-004: 간편 로그인 (P0)
> **As a** 회원가입이 귀찮은 사용자,
> **I want to** Google 계정으로 바로 로그인할 수 있기를,
> **So that** 회원가입 양식을 채울 필요 없이 서비스를 쓴다.

**Acceptance Criteria**:
- [ ] 메인 페이지 "Google로 로그인" 버튼 클릭 → Google OAuth 동의 화면
- [ ] 최초 로그인 시 자동으로 회원가입 처리
- [ ] 이후 재로그인 시 동일 계정으로 식별
- [ ] 토큰 만료 시 자동 재로그인 유도

### US-005: 모바일 우선 사용 경험 (P1)
> **As a** 스마트폰 사용자,
> **I want to** 모바일 화면에서도 모든 기능이 자연스럽게 동작하기를,
> **So that** PC 없이도 쇼핑 가능하다.

**Acceptance Criteria**:
- [ ] 모든 버튼이 터치하기 쉬운 크기 (≥44px)
- [ ] iOS 입력 시 자동 줌 안 됨
- [ ] 햄버거 메뉴로 주요 페이지 이동
- [ ] 가로 스크롤 없음

---

## 9. Success Metrics (KPI)

### 9.1 Product KPI
| 지표 | 목표 | 측정 방법 |
|-----|-----|----------|
| **피팅 생성 성공률** | ≥ 90% | `VirtualFitting.status` (COMPLETED / 전체) |
| **피팅 사용률 (DAU 대비)** | ≥ 30% | 피팅 요청 수 / 활성 사용자 |
| **피팅 → 장바구니 전환율** | ≥ 15% | 피팅 후 장바구니 추가 세션 비율 |
| **피팅 → 주문 전환율** | ≥ 5% | 피팅 후 주문 완료 세션 비율 |
| **평균 피팅 생성 시간** | < 60s (p95) | 요청 → COMPLETED 타임스탬프 차이 |
| **리뷰 작성률** | ≥ 20% | 리뷰 수 / 배송 완료 주문 수 |

### 9.2 Technical KPI
| 지표 | 목표 |
|-----|-----|
| API p95 응답 시간 (피팅 제외) | < 500ms |
| 검색 API p95 응답 시간 | < 300ms |
| 에러율 (5xx) | < 0.5% |
| ES 인덱스 동기화 lag | < 10초 |
| 가동 시간 (uptime) | ≥ 99% |

---

## 10. 제약 및 의존성 (Constraints & Dependencies)

### 10.1 기술 제약
- **AI 모델**: Hugging Face IDM-VTON (무료 플랜) — GPU 할당량 제한 있음
- **Python 런타임**: 서버에 Python 3.x + `gradio_client` 설치 필수
- **Elasticsearch**: 앱 기동 시 ES 필수 (`InitialIndexLoader`) ← 단일 장애점
- **로컬 파일 시스템 의존**: 업로드·피팅 결과 이미지가 EC2 디스크에 저장 → 수평 확장 불가
- **Java 21 LTS**: 빌드/런타임 환경 고정

### 10.2 외부 의존성
| 서비스 | 용도 | 장애 영향 |
|-------|------|----------|
| Hugging Face Space | AI 피팅 | 피팅 기능 전체 중단 |
| Google OAuth2 | 로그인 | 신규/재로그인 불가 (기존 토큰은 유효) |
| AWS EC2 | 백엔드 호스팅 | 전체 서비스 중단 |
| Vercel | 프론트엔드 호스팅 | 정적 자산 서빙 중단 |
| Docker Hub | 이미지 저장소 | 배포 불가 (기존 서비스는 정상) |

### 10.3 비즈니스 제약
- **다국어 미지원** — 한국어 Nori 분석기 선택으로 인해 현재는 한국어에 최적화
- **결제 미구현** — 주문은 생성되지만 실제 결제 미완료 (내부 데모 수준)
- **B2B 판매자 온보딩 없음** — 단일 운영자 전제의 카탈로그

---

## 11. 주요 리스크 & 가정

### 11.1 High Severity Risks
| 리스크 | 영향 | 완화 방안 |
|-------|------|-------|
| **Hugging Face GPU quota 초과** | 피팅 불가 → 핵심 가치 상실 | 유료 플랜 전환, 또는 자체 GPU 호스팅 검토 |
| **로컬 FS 의존 → 배포 시 이미지 유실** | 사용자 사진·피팅 결과 유실 | S3 마이그레이션 **Critical Path** |
| **ES 없으면 서버 기동 실패** | 배포 초기 부팅 오류 | `InitialIndexLoader`의 fail-soft 처리 |
| **Python 프로세스 호출이 트랜잭션 내부에 있으면 DB 커넥션 풀 고갈** | 전체 DB 응답 불가 | `@Transactional` 경계 검토 (CLAUDE.md 규칙) |
| **`@Builder.Default` 사용 패턴 재발** | 엔티티 timestamp null → NPE | `BaseEntity` 도입 또는 명시적 초기화 |
| **JWT를 `localStorage`에 저장 → XSS 취약** | 토큰 탈취 시 계정 탈취 | `HttpOnly Cookie` 전환 검토 |

### 11.2 Medium Severity Risks
- **AVIF → JPEG 변환이 프론트엔드에서 실패하면 업로드 불가** → 서버 측 변환 fallback 필요
- **검색 인덱스 매핑 변경 시 자동 마이그레이션 불가** → 수동 삭제 후 재기동 필요
- **리뷰 이미지를 로컬 FS에 저장 → 대용량 스토리지 필요**

### 11.3 가정 (Assumptions)
- 사용자는 본인 사진을 업로드할 의사가 있다
- Google OAuth2가 대다수 한국 사용자에게 충분하다
- Hugging Face 무료 플랜으로 MVP 검증이 가능하다
- 운영 초기 트래픽은 단일 EC2 인스턴스로 감당 가능하다

---

## 12. Open Questions (사용자 검토 필요)

다음 항목은 현재 코드/문서로 판단할 수 없어 **사용자 결정이 필요**하다.

1. **결제 연동 계획이 있는가?**
   - 현재 주문 플로우는 재고 차감 + 기록만 있음
   - PG 연동 전까지는 "데모 커머스" 수준
   - 계획이 있다면 어떤 PG사? (토스, 카카오페이, PortOne 등)

2. **사용자 사진·피팅 결과의 데이터 보존 정책은?**
   - 현재: 로컬 FS에 영구 저장
   - 필요: 삭제 요청 처리, 일정 기간 후 자동 삭제, 익명화 등

3. **B2B 판매자 온보딩 계획이 있는가?**
   - 있다면 관리자 대시보드·정산·수수료 구조가 필요

4. **AI 코디 추천은 구현할 것인가?**
   - CLAUDE.md의 API 목록에 `/api/v1/recommend/outfit`로 표시되어 있지만 미구현
   - 체형 분석 + 스타일 매칭이 필요하면 별도 AI 모델 선정 필요

5. **JWT 저장소를 `localStorage`에서 `HttpOnly Cookie`로 전환할 것인가?**
   - XSS 위험 감소 vs. CSRF 보호 및 CORS 설정 추가 비용의 트레이드오프

6. **S3 마이그레이션 시점은 언제인가?**
   - 현재 Critical Debt 1순위
   - 프로덕션 배포 **전에** 해야 하는가, 이후에 해도 되는가?

7. **다국어 지원 계획이 있는가?**
   - 있다면 Elasticsearch 분석기를 다국어용으로 재설계해야 함

8. **관리자 대시보드 필요성**
   - 현재 ADMIN API만 일부 있음
   - 프론트엔드 UI가 필요한가?

---

## 13. 로드맵 제안 (Claude의 제안 — 승인 필요)

현재 main 브랜치의 untracked 파일이 배포 준비 작업임을 감안해, 다음 순서로 진행 제안:

### Phase 5.1 — QA & 안정화 (현재 진행 중)
- [x] JWT 인증 컨트롤러 일관성 정리 (커밋 완료)
- [x] ReviewForm Authorization 헤더 수정 (커밋 완료)
- [ ] E2E 테스트 전체 재실행
- [ ] 부하 테스트 (피팅, 검색 중심)

### Phase 5.2 — Critical Debt 해결 (배포 전 필수)
- [ ] **S3 마이그레이션** (`NFR-SCALE-001`, `NFR-AVAIL` 핵심)
- [ ] `InitialIndexLoader` fail-soft 처리 (`NFR-AVAIL-001`)
- [ ] `@Builder.Default` 제거 및 `BaseEntity` 도입 검토
- [ ] `@Setter` 제거 (Review, Cart 등) — 신규 코드부터 강제

### Phase 6 — 프로덕션 배포
- [ ] GitHub Actions CI/CD 완성
- [ ] Vercel 프론트엔드 배포
- [ ] EC2 + Docker Compose 백엔드 배포
- [ ] HTTPS + 도메인 연결
- [ ] 기본 모니터링 대시보드 (Grafana)

### Phase 7 — Post-MVP 기능 확장
- [ ] 결제 연동 (PG 선정 후)
- [ ] 관리자 대시보드
- [ ] AI 코디 추천
- [ ] 다중 소셜 로그인 (Kakao, Naver)
- [ ] Redis 캐싱

---

## 14. 문서 이력

| 버전 | 날짜 | 작성자 | 변경 사항 |
|-----|------|------|--------|
| 1.0 | 2026-04-10 | Claude Code | 초안 작성 — CLAUDE.md, 코드베이스, git 상태로부터 역추출 |

---

## 부록 A — API 엔드포인트 매트릭스 (FR ↔ API)

| FR ID | HTTP Method | Endpoint | 인증 |
|-------|------------|----------|------|
| FR-AUTH-001 | GET | `/oauth2/authorization/google` | - |
| FR-AUTH-002 | GET | `/login/success` | - |
| FR-PRODUCT-001,002 | GET | `/api/v1/products` | - |
| FR-PRODUCT-003 | GET | `/api/v1/products/{pID}` | - |
| FR-SEARCH-001~004 | GET | `/api/v1/search` | - |
| FR-SEARCH-005 | GET | `/api/v1/search/suggestions` | - |
| FR-SEARCH-007 | POST | `/api/v1/admin/search/reindex` | ADMIN |
| FR-CART-001 | POST | `/api/v1/cart` | ✅ |
| FR-CART-004 | PATCH | `/api/v1/cart/{pID}` | ✅ |
| FR-CART-005 | DELETE | `/api/v1/cart/{pID}` | ✅ |
| FR-CART-006 | GET | `/api/v1/cart` | ✅ |
| FR-WISHLIST-001 | POST | `/api/v1/wishlist` | ✅ |
| FR-WISHLIST-003 | DELETE | `/api/v1/wishlist/{productId}` | ✅ |
| FR-WISHLIST-004 | GET | `/api/v1/wishlist` | ✅ |
| FR-ORDER-001 | POST | `/api/v1/orders` | ✅ |
| FR-ORDER-003 | GET | `/api/v1/orders` | ✅ |
| FR-ORDER-004 | GET | `/api/v1/orders/{orderno}` | ✅ |
| FR-REVIEW-001 | POST | `/api/v1/products/{productId}/reviews` | ✅ |
| FR-REVIEW-008 | GET | `/api/v1/products/{productId}/reviews` | - |
| FR-REVIEW-009 | GET | `/api/v1/products/{productId}/reviews/summary` | - |
| FR-REVIEW-007 | PATCH | `/api/v1/reviews/{reviewId}` | ✅ |
| FR-REVIEW-006 | DELETE | `/api/v1/reviews/{reviewId}` | ✅ |
| FR-FITTING-001,002 | POST | `/api/v1/fitting/upload` | ✅ |
| FR-FITTING-005 | POST | `/api/v1/fitting/generate` | ✅ |
| FR-FITTING-006 | GET | `/api/v1/fitting/status/{fittingId}` | ✅ |
| FR-FITTING-009 | GET | `/api/v1/fitting/history` | ✅ |

---

**END OF DOCUMENT**
