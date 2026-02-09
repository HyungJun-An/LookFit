# LookFit - AI 기반 가상 착장샷 서비스

> **차별점**: 일반 쇼핑몰이 아닌 AI 착장샷 서비스
> 사용자 사진 + 선택한 옷 = AI가 착장샷 생성

---

## 🎯 프로젝트 개요

**LookFit**은 AI 기술을 활용한 가상 피팅 서비스입니다. 사용자가 자신의 사진을 업로드하고 원하는 상품을 선택하면, AI가 해당 옷을 입은 모습을 생성해줍니다.

### 핵심 기능
1. **AI 가상 피팅** - 사용자 사진에 옷을 입혀주는 가상 착장샷 생성
2. **상품 검색** - Elasticsearch 기반 한글 검색 (Nori 형태소 분석기)
3. **찜 목록** - 마음에 드는 상품 저장
4. **장바구니 & 주문** - 재고 관리, 트랜잭션 처리
5. **OAuth2 로그인** - Google 소셜 로그인 + JWT 인증

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

### Infrastructure
- **Docker** - MySQL, Elasticsearch 컨테이너
- **AWS S3** (예정) - 이미지 저장소

### AI (예정)
- **Hugging Face Inference API** - 가상 피팅 (IDM-VTON 모델)

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
└── fitting/                   # AI 가상 피팅 도메인 (예정)
    ├── domain/               # VirtualFitting
    ├── repository/
    ├── service/
    ├── dto/
    └── controller/
```

### 프론트엔드
```
frontend/src/
├── api/                      # API 클라이언트
│   └── axiosInstance.ts     # Axios 인터셉터 (JWT 자동 추가)
├── components/               # React 컴포넌트
│   ├── Header.tsx
│   ├── ProductList.tsx
│   ├── ProductDetail.tsx
│   ├── Cart.tsx
│   ├── Login.tsx
│   ├── LoginSuccess.tsx
│   └── VirtualFitting.tsx   # AI 피팅 (예정)
├── pages/                    # 페이지 컴포넌트
│   ├── SearchResults.tsx
│   ├── Wishlist.tsx
│   └── Signup.tsx
├── context/                  # React Context
│   └── AuthContext.tsx      # 인증 상태 관리
├── types/                    # TypeScript 타입
│   ├── product.ts
│   ├── cart.ts
│   └── wishlist.ts
├── utils/                    # 유틸리티
│   └── imageUtils.ts        # 이미지 URL 처리
└── styles/                   # CSS
```

---

## 🚀 시작하기

### 사전 요구사항
- Java 21
- Node.js 18+
- Docker & Docker Compose
- MySQL 8.0
- Elasticsearch 8.17.0

### 1. 저장소 클론
```bash
git clone https://github.com/HyungJun-An/LookFit.git
cd LookFit
```

### 2. 환경변수 설정

#### 백엔드 (`backend/src/main/resources/application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lookfit_db
    username: root
    password: 651212

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

---

## 📊 데이터베이스 스키마

### 주요 테이블
- **member** - 회원 정보
- **social_account** - OAuth2 소셜 계정
- **product** - 상품 정보
- **cart** - 장바구니
- **buy** - 주문
- **order_item** - 주문 상품
- **wishlist** - 찜 목록
- **search_log** - 검색 로그
- **virtual_fitting** (예정) - AI 피팅 기록

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

### AI 가상 피팅 (예정)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/fitting/upload` | 사용자 사진 업로드 | ✅ |
| POST | `/api/v1/fitting/generate` | AI 피팅 생성 요청 | ✅ |
| GET | `/api/v1/fitting/{fittingId}` | 피팅 결과 조회 | ✅ |
| GET | `/api/v1/fitting/history` | 내 피팅 기록 | ✅ |

---

## 🧪 테스트

### 백엔드 테스트
```bash
./gradlew test
```

### E2E 테스트
```bash
cd e2e-tests
npm install
npm test
```

**테스트 커버리지**: 51개 테스트 100% 통과
- Shopping Flow: 9/9
- Product Detail: 13/13
- Order API: 7/7
- Image Loading: 8/9
- Cart Flow: 4/4
- Search Flow: 10/10

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

### ✅ Phase 4: 이미지 관리 시스템 (완료)
- [x] 상품 이미지 다운로드 스크립트
- [x] 로컬 이미지 저장 구조
- [x] 프론트엔드 이미지 URL 처리
- [x] 환경변수 설정 (.env)
- [x] S3 마이그레이션 준비

### 🔄 Phase 5: AI 가상 피팅 (진행 중)
- [ ] Hugging Face API 연동
- [ ] VirtualFitting 도메인 구현
- [ ] 사용자 이미지 업로드
- [ ] AI 피팅 생성 (비동기)
- [ ] 피팅 결과 표시
- [ ] 피팅 히스토리

### 📅 Phase 6: 최적화 & 배포 (예정)
- [ ] 이미지 S3 마이그레이션
- [ ] Redis 캐싱
- [ ] CI/CD 파이프라인
- [ ] AWS 배포 (EC2, RDS)
- [ ] 성능 최적화

---

## 🎨 주요 기능 스크린샷

### 1. 상품 목록
- 카테고리 필터
- 정렬 옵션 (최신순, 인기순, 가격순)
- 찜하기 버튼
- AI 착장샷 버튼

### 2. 상품 검색
- 키워드 검색 (한글 형태소 분석)
- 카테고리 필터
- 가격 정렬
- 인기/최근 검색어 추천

### 3. 장바구니
- 상품 수량 변경
- 개별 삭제
- 총 금액 계산
- 재고 확인

### 4. AI 가상 피팅 (예정)
- 사용자 사진 업로드
- 상품 선택
- AI 피팅 생성
- 결과 이미지 표시

---

## 📖 문서

- [CLAUDE.md](./CLAUDE.md) - 프로젝트 개요, 에이전트 협업 규칙
- [NAMING_CONVENTION.md](./NAMING_CONVENTION.md) - 네이밍 규칙
- [docs/AI_FITTING_PLAN_HUGGINGFACE.md](./docs/AI_FITTING_PLAN_HUGGINGFACE.md) - AI 피팅 구현 계획
- [docs/DAILY_LOG_2026_02_09.md](./docs/DAILY_LOG_2026_02_09.md) - 오늘 작업 내역

---

## 🐛 알려진 이슈

### 해결 완료
- ~~OAuth2 로그인 500 에러~~ ✅ (2026-02-09)
- ~~한글 인코딩 깨짐~~ ✅ (2026-02-01)
- ~~REST API 401 처리~~ ✅ (2026-02-01)
- ~~이미지 placeholder 표시~~ ✅ (2026-02-09)
- ~~JWT 토큰 만료 시 처리 안됨~~ ✅ (2026-02-09)

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
- GitHub: [@HyungJun-An](https://github.com/HyungJun-An)
- Email: wns1265@gmail.com

---

## 🙏 감사의 말

- **Claude AI** - 개발 어시스턴트
- **Spring Boot** - 백엔드 프레임워크
- **React** - 프론트엔드 라이브러리
- **Elasticsearch** - 검색 엔진
- **Hugging Face** - AI 모델 제공

---

**마지막 업데이트**: 2026-02-09
