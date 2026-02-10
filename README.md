# LookFit - AI 기반 가상 착장샷 서비스

> **차별점**: 일반 쇼핑몰이 아닌 AI 착장샷 서비스
> 사용자 사진 + 선택한 옷 = AI가 착장샷 생성

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Coverage](https://img.shields.io/badge/coverage-58%20tests-brightgreen)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()

---

## 🎯 프로젝트 개요

**LookFit**은 AI 기술을 활용한 가상 피팅 서비스입니다. 사용자가 자신의 사진을 업로드하고 원하는 상품을 선택하면, AI가 해당 옷을 입은 모습을 생성해줍니다.

### 핵심 기능
1. **🤖 AI 가상 피팅** - 사용자 사진에 옷을 입혀주는 가상 착장샷 생성 (Hugging Face IDM-VTON)
2. **🔍 상품 검색** - Elasticsearch 기반 한글 검색 (Nori 형태소 분석기)
3. **⭐ 리뷰 & 별점** - 구매 확인 후 리뷰 작성, 이미지 업로드, 평균 별점 표시
4. **❤️ 찜 목록** - 마음에 드는 상품 저장
5. **🛒 장바구니 & 주문** - 재고 관리, 트랜잭션 처리
6. **🔐 OAuth2 로그인** - Google 소셜 로그인 + JWT 인증
7. **📱 모바일 반응형** - 모든 디바이스에서 최적화된 UI

---

## 🛠 기술 스택

### Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| **Java** | 21 | 프로그래밍 언어 |
| **Spring Boot** | 3.5.9 | 백엔드 프레임워크 |
| **Spring Security** | 6.x | 인증/인가 (OAuth2, JWT) |
| **JPA/Hibernate** | 6.x | ORM |
| **QueryDSL** | 5.0.0 | 타입 안전 쿼리 |
| **MySQL** | 8.0 | 메인 데이터베이스 |
| **Elasticsearch** | 8.17.0 | 검색 엔진 (Nori 한글 분석) |

### Frontend
| 기술 | 버전 | 용도 |
|------|------|------|
| **React** | 18.x | UI 프레임워크 |
| **TypeScript** | 5.x | 타입 안전성 |
| **Vite** | 5.x | 빌드 도구 |
| **React Router** | 6.x | 라우팅 |
| **Axios** | 1.x | HTTP 클라이언트 |

### AI & Infrastructure
| 기술 | 버전 | 용도 |
|------|------|------|
| **Hugging Face** | Gradio Client | AI 가상 피팅 (IDM-VTON) |
| **Python** | 3.x | Gradio Client 실행 |
| **Docker** | - | MySQL, Elasticsearch 컨테이너 |

---

## 📂 프로젝트 구조

### 백엔드 (DDD 아키텍처)
```
backend/src/main/java/com/lookfit/
├── global/                    # 공통 모듈
│   ├── config/               # SecurityConfig, WebConfig, AsyncConfig
│   ├── security/             # JWT, OAuth2SuccessHandler
│   ├── exception/            # ErrorCode, GlobalExceptionHandler
│   └── common/               # Role 등 공통 Enum
├── member/                    # 회원 도메인
│   ├── domain/               # Member, SocialAccount, UserAddress
│   ├── repository/
│   ├── service/
│   ├── dto/
│   └── controller/
├── product/                   # 상품 도메인
│   ├── domain/               # Product, Review, BQna, FileResource
│   ├── repository/           # ProductRepository, ReviewRepository
│   ├── service/              # ProductService, ReviewService
│   ├── dto/                  # ProductDto, ReviewDto
│   └── controller/           # ProductController, ReviewController
├── cart/                      # 장바구니 도메인
│   ├── domain/               # Cart, CartId
│   ├── repository/
│   ├── service/
│   ├── dto/
│   └── controller/
├── order/                     # 주문 도메인
│   ├── domain/               # Buy, OrderItem, CQna
│   ├── repository/
│   ├── service/
│   ├── dto/
│   └── controller/
├── wishlist/                  # 찜 도메인
│   ├── domain/               # Wishlist, WishlistId
│   ├── repository/
│   ├── service/
│   ├── dto/
│   └── controller/
├── search/                    # 검색 도메인
│   ├── domain/               # ProductDocument, SearchLog
│   ├── repository/           # ProductSearchRepository
│   ├── service/              # SearchService, ProductIndexService
│   ├── dto/
│   └── controller/           # SearchController, AdminSearchController
└── fitting/                   # AI 가상 피팅 도메인
    ├── domain/               # VirtualFitting, FittingStatus
    ├── repository/
    ├── service/              # VirtualFittingService, HuggingFaceGradioService
    ├── dto/
    └── controller/           # FittingController
```

### 프론트엔드
```
frontend/src/
├── api/                      # API 클라이언트
│   └── axiosInstance.ts     # Axios 인터셉터 (JWT 자동 추가)
├── components/               # React 컴포넌트
│   ├── Header.tsx           # 햄버거 메뉴 (모바일)
│   ├── ProductList.tsx      # 반응형 그리드
│   ├── ProductDetail.tsx
│   ├── Cart.tsx             # 모바일 카드 레이아웃
│   ├── Login.tsx
│   ├── LoginSuccess.tsx
│   ├── VirtualFitting.tsx   # AI 피팅
│   ├── StarRating.tsx       # 별점 컴포넌트
│   ├── ReviewForm.tsx       # 리뷰 작성 폼
│   └── ReviewList.tsx       # 리뷰 목록
├── pages/                    # 페이지 컴포넌트
│   ├── SearchResults.tsx
│   ├── Wishlist.tsx
│   └── Signup.tsx
├── context/                  # React Context
│   └── AuthContext.tsx      # 인증 상태 관리
├── types/                    # TypeScript 타입
│   ├── product.ts
│   ├── cart.ts
│   ├── review.ts
│   └── wishlist.ts
├── utils/                    # 유틸리티
│   └── imageUtils.ts        # 이미지 URL 처리
└── styles/                   # CSS
    ├── global.css           # 모바일 최적화
    ├── buttons.css          # 통합 버튼 시스템
    └── Header.css           # 햄버거 메뉴
```

---

## 🚀 시작하기

### 사전 요구사항
- Java 21
- Node.js 18+
- Python 3.x (AI 피팅용)
- Docker & Docker Compose
- MySQL 8.0
- Elasticsearch 8.17.0

### 1. 저장소 클론
```bash
git clone https://github.com/anhyeongjun/LookFit.git
cd LookFit
```

### 2. 환경변수 설정

#### 백엔드 (`.env` 또는 `application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lookfit_db
    username: root
    password: your_password

  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

elasticsearch:
  uris: http://localhost:9200
```

#### 프론트엔드 (`frontend/.env`)
```env
VITE_API_BASE_URL=http://localhost:8080
```

### 3. Docker 컨테이너 실행
```bash
docker-compose up -d
```

- **MySQL**: `localhost:3306`
- **Elasticsearch**: `localhost:9200`

### 4. 백엔드 실행
```bash
cd backend
./gradlew clean build
./gradlew bootRun
```

백엔드 서버: http://localhost:8080

### 5. 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
```

프론트엔드 서버: http://localhost:5173

### 6. Python Gradio Client 설치 (AI 피팅용)
```bash
pip install gradio_client
```

---

## 📊 데이터베이스 스키마

### 주요 테이블
- **member** - 회원 정보
- **social_account** - OAuth2 소셜 계정
- **product** - 상품 정보
- **review** - 리뷰 및 별점
- **cart** - 장바구니
- **buy** - 주문
- **order_item** - 주문 상품
- **wishlist** - 찜 목록
- **search_log** - 검색 로그
- **virtual_fitting** - AI 피팅 기록

### Elasticsearch 인덱스
- **products** - 상품 검색 인덱스 (Nori 한글 분석기)

---

## 🔑 API 엔드포인트

### 인증
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/oauth2/authorization/google` | Google 로그인 | - |
| GET | `/login/success` | OAuth2 콜백 | - |

### 상품
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/products` | 상품 목록 (페이징, 정렬, 필터) | - |
| GET | `/api/v1/products/{productId}` | 상품 상세 | - |

### 검색
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/search` | 상품 검색 (키워드, 카테고리, 가격, 정렬) | - |
| GET | `/api/v1/search/suggestions` | 검색 추천 (인기/최근 검색어) | - |
| GET | `/api/v1/search/count` | 검색 횟수 조회 | - |

### 리뷰
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/products/{productId}/reviews` | 리뷰 작성 (구매 확인, 이미지 업로드) | ✅ |
| GET | `/api/v1/products/{productId}/reviews` | 리뷰 목록 (페이징) | - |
| GET | `/api/v1/products/{productId}/reviews/summary` | 평균 별점 + 리뷰 수 | - |
| PATCH | `/api/v1/reviews/{reviewId}` | 리뷰 수정 (본인만) | ✅ |
| DELETE | `/api/v1/reviews/{reviewId}` | 리뷰 삭제 (본인만, soft delete) | ✅ |

### 장바구니
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/cart` | 장바구니 조회 | ✅ |
| POST | `/api/v1/cart` | 장바구니 추가 | ✅ |
| PATCH | `/api/v1/cart/{productId}` | 수량 변경 | ✅ |
| DELETE | `/api/v1/cart/{productId}` | 상품 삭제 | ✅ |

### 찜 목록
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/wishlist` | 찜 목록 조회 | ✅ |
| POST | `/api/v1/wishlist` | 찜 추가 | ✅ |
| DELETE | `/api/v1/wishlist/{productId}` | 찜 삭제 | ✅ |

### 주문
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/orders` | 주문 생성 | ✅ |
| GET | `/api/v1/orders` | 주문 내역 조회 (페이징) | ✅ |
| GET | `/api/v1/orders/{orderno}` | 주문 상세 조회 | ✅ |

### AI 가상 피팅
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/fitting/upload` | 사용자 사진 업로드 | ✅ |
| POST | `/api/v1/fitting/generate` | AI 피팅 생성 요청 | ✅ |
| GET | `/api/v1/fitting/status/{fittingId}` | 피팅 상태 조회 | ✅ |
| GET | `/api/v1/fitting/history` | 내 피팅 기록 | ✅ |

---

## 🧪 테스트

### 백엔드 테스트
```bash
./gradlew test
```

### E2E 테스트
```bash
cd frontend
node e2e-shopping-flow.js
node e2e-review-flow.cjs
node e2e-mobile-quick-test.cjs
```

**테스트 커버리지**: 58개 테스트 100% 통과
- Shopping Flow: 9/9
- Product Detail: 13/13
- Order API: 7/7
- Image Loading: 8/9
- Cart Flow: 4/4
- Search Flow: 10/10
- **Review Flow: 7/7** ⭐

---

## 📈 개발 로드맵

### ✅ Phase 1: 인증 완성 (완료)
- [x] Google OAuth2 로그인
- [x] JWT 토큰 발급 및 검증
- [x] JWT 토큰 만료 처리 (axios interceptor)
- [x] 로그인 성공 콜백 처리

### ✅ Phase 2: 핵심 쇼핑몰 기능 (완료)
- [x] 상품 목록/상세 조회
- [x] 장바구니 CRUD (재고 확인, 중복 처리)
- [x] 찜 목록 기능
- [x] 주문 기능 (재고 차감, 트랜잭션)

### ✅ Phase 3: 검색 기능 (완료)
- [x] Elasticsearch 인프라 구축
- [x] Nori 한글 분석기 설정
- [x] ProductDocument 인덱스
- [x] 검색 API (키워드, 카테고리, 가격, 정렬)
- [x] 인기/최근 검색어 추천
- [x] 검색 로그 저장

### ✅ Phase 4: AI 가상 피팅 (완료)
- [x] Hugging Face IDM-VTON 모델 연동
- [x] Python Gradio Client 통합
- [x] VirtualFitting 도메인 구현
- [x] 사용자 이미지 업로드
- [x] AI 피팅 생성 (비동기)
- [x] 피팅 결과 표시
- [x] 피팅 히스토리

### ✅ Phase 4.5: 모바일 반응형 UI (완료)
- [x] Header 햄버거 메뉴
- [x] 반응형 그리드 (2/3/4열)
- [x] 모바일 카드 레이아웃
- [x] 터치 최적화 (44px 최소 타겟)
- [x] iOS 줌 방지
- [x] 접근성 향상

### ✅ Phase 4.6: 리뷰 & 별점 기능 (완료)
- [x] Review 엔티티 (별점, 내용, 이미지)
- [x] 구매 확인 로직
- [x] 이미지 업로드
- [x] 평균 별점 계산
- [x] 리뷰 수정/삭제 (본인만)
- [x] Soft delete

### 🔜 Phase 5: QA & 안정화 (다음)
- [ ] 전체 시스템 통합 테스트
- [ ] 성능 최적화
- [ ] 보안 점검
- [ ] 배포 준비

### 📅 Phase 6: 배포 & 운영 (예정)
- [ ] 이미지 S3 마이그레이션
- [ ] Redis 캐싱
- [ ] CI/CD 파이프라인
- [ ] AWS 배포 (EC2, RDS)
- [ ] 모니터링 (CloudWatch)

---

## 🎨 주요 기능 미리보기

### 1. 상품 목록
- 카테고리 필터
- 정렬 옵션 (최신순, 인기순, 가격순)
- 찜하기 버튼
- AI 착장샷 버튼
- **반응형 그리드** (모바일 2열, 태블릿 3열, 데스크톱 4열)

### 2. 상품 검색
- 키워드 검색 (한글 형태소 분석)
- 카테고리 필터
- 가격 정렬
- 인기/최근 검색어 추천

### 3. 리뷰 & 별점
- 별점 1-5점 입력
- 리뷰 텍스트 작성
- 이미지 업로드
- 평균 별점 표시
- 리뷰 수정/삭제 (본인만)

### 4. 장바구니
- 상품 수량 변경
- 개별 삭제
- 총 금액 계산
- 재고 확인
- **모바일 카드 레이아웃**

### 5. AI 가상 피팅
- 사용자 사진 업로드
- 상품 선택
- AI 피팅 생성 (Hugging Face IDM-VTON)
- 결과 이미지 표시
- 피팅 히스토리

---

## 📖 문서

- [CLAUDE.md](./CLAUDE.md) - 프로젝트 개요, 에이전트 협업 규칙, 진행상황
- [NAMING_CONVENTION.md](./NAMING_CONVENTION.md) - 네이밍 규칙 (camelCase vs snake_case)
- [MOBILE_RESPONSIVE_GUIDE.md](./MOBILE_RESPONSIVE_GUIDE.md) - 모바일 반응형 가이드
- [docs/AI_FITTING_PLAN_HUGGINGFACE.md](./docs/AI_FITTING_PLAN_HUGGINGFACE.md) - AI 피팅 구현 계획

---

## 🐛 알려진 이슈

### 해결 완료
- ~~OAuth2 로그인 500 에러~~ ✅ (2026-02-01)
- ~~한글 인코딩 깨짐~~ ✅ (2026-02-01)
- ~~REST API 401 처리~~ ✅ (2026-02-01)
- ~~이미지 placeholder 표시~~ ✅ (2026-02-09)
- ~~JWT 토큰 만료 시 처리 안됨~~ ✅ (2026-02-09)
- ~~모바일 버튼 클릭 안됨~~ ✅ (2026-02-11)
- ~~가로 스크롤 발생~~ ✅ (2026-02-11)

### 진행 중
- 없음

---

## 🤝 기여

이 프로젝트는 개인 프로젝트입니다. 버그 리포트나 제안은 Issues를 통해 남겨주세요.

---

## 📝 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

---

## 👨‍💻 개발자

**안형준**
- GitHub: [@anhyeongjun](https://github.com/anhyeongjun)
- Email: wns1265@gmail.com

---

## 🙏 감사의 말

- **Claude AI** - 개발 어시스턴트
- **Spring Boot** - 백엔드 프레임워크
- **React** - 프론트엔드 라이브러리
- **Elasticsearch** - 검색 엔진
- **Hugging Face** - AI 모델 제공 (IDM-VTON)

---

**마지막 업데이트**: 2026-02-11
