# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 프로젝트 개요

**LookFit** — AI 가상 착장샷 커머스 플랫폼.

일반 쇼핑몰이 아니라, 사용자가 자기 사진과 상품을 고르면 AI가 착용 이미지를 생성해 주는 것이 핵심 차별점이다. 일반 쇼핑몰 기능(상품·장바구니·주문·리뷰·찜·검색)은 AI 가상 피팅(Hugging Face IDM-VTON)을 위한 토대다.

### 주요 기능
1. **AI 가상 착장샷 생성** — 사용자 사진 + 상품 → Hugging Face IDM-VTON 모델로 착장 이미지 생성
2. **상품 검색** — Elasticsearch + Nori 한글 형태소 분석기
3. **리뷰 & 별점** — 구매 확인 기반, 1인 1상품 1리뷰, soft delete
4. **장바구니 · 주문** — 재고 확인, 트랜잭션 기반 재고 차감
5. **찜 목록** — 복합키 기반 저장
6. **OAuth2 로그인** — Google 소셜 로그인 + JWT
7. **모바일 반응형 UI** — 44px 최소 터치 타겟, iOS 줌 방지

---

## 기술 스택 및 버전

### Backend
| 분류 | 기술 | 버전 |
|------|------|------|
| Language | Java | 21 (LTS) |
| Framework | Spring Boot | 3.5.9 |
| ORM | Spring Data JPA (Hibernate) | 6.x |
| Query | QueryDSL | 5.0.0 (jakarta classifier) |
| Security | Spring Security + OAuth2 Client | 6.x |
| JWT | jjwt | 0.12.5 |
| Database | MySQL | 8.0 |
| Search | Elasticsearch + Nori | 8.17.0 |
| Monitoring | Actuator + Micrometer (Prometheus) | — |
| Mapper | ModelMapper | 3.2.0 |
| Build | Gradle (Kotlin DSL) | 8.x |
| Test | JUnit 5, Spring Security Test, H2 | — |

### Frontend
| 분류 | 기술 | 버전 |
|------|------|------|
| Framework | React | 19.2 |
| Language | TypeScript | 5.9 |
| Build Tool | Vite (rolldown-vite) | 7.2 |
| Routing | React Router | 7.13 |
| HTTP | Axios | 1.13 |
| E2E | Puppeteer | 24.x |

### AI & Infra
| 분류 | 기술 | 용도 |
|------|------|------|
| AI Model | Hugging Face IDM-VTON (`yisol/IDM-VTON` Space) | 가상 피팅 |
| AI Client | Python `gradio_client` | Hugging Face Space 호출 |
| Bridge | `ProcessBuilder` → `backend/scripts/virtual_tryon.py` | Java ↔ Python |
| Container | Docker Compose | MySQL, Elasticsearch |

> ⚠️ `build.gradle.kts`에 `com.google.genai:google-genai:1.36.0` 의존성이 남아 있지만, **현재는 사용되지 않는다**. Gemini 기반 피팅 시도가 중단되고 Hugging Face 루트로 확정되면서 남은 잔재다. 새 코드에서 Gemini 경로를 살리고 싶다면 먼저 사용자에게 확인할 것.

---

## 프로젝트 구조

### 저장소 레이아웃
```
LookFit/
├── backend/                          # Spring Boot 서버
│   ├── src/main/java/com/lookfit/
│   │   ├── global/                  # 공통 모듈
│   │   └── {domain}/                # DDD Bounded Context
│   ├── src/main/resources/
│   ├── scripts/                     # Python 브릿지 스크립트
│   │   └── virtual_tryon.py        # Hugging Face Gradio Client 호출
│   └── build.gradle.kts
├── frontend/                         # React + Vite SPA
│   └── src/
├── docker-compose.yml               # MySQL + Elasticsearch 로컬 인프라
├── docker-compose.prod.yml
├── TROUBLESHOOTING.md                # 재발 방지 이슈 8건 (OAuth2, 인코딩, 401, ES, 리뷰, E2E, OAuth redirect URI)
├── DEPLOYMENT_GUIDE.md               # EC2 + GitHub Actions + 대안 배포 (Vercel/Cloudflare/환경변수/OAuth 설정)
└── docs/
    ├── PRD.md                       # 제품 요구사항 정의서 (FR/NFR, Open Questions)
    └── AI_FITTING_PLAN_HUGGINGFACE.md  # Hugging Face IDM-VTON 연동 설계
```

### 백엔드 DDD Bounded Context
패키지 루트는 `com.lookfit`. 각 도메인은 독립된 바운디드 컨텍스트이며, `global/`만 공유 참조가 허용된다.

| Domain     | 책임                                                                  | 주요 엔티티/컴포넌트                                     |
|------------|---------------------------------------------------------------------|-----------------------------------------------------|
| `global`   | 보안·예외·설정의 공통 인프라                                                   | `SecurityConfig`, `JwtAuthenticationFilter`, `OAuth2SuccessHandler`, `JwtTokenProvider`, `GlobalExceptionHandler`, `ErrorCode`, `BusinessException`, `AsyncConfig`, `WebConfig` |
| `member`   | 회원 식별 (OAuth2)                                                      | `Member`, `SocialAccount`, `UserAddress`            |
| `product`  | 상품 카탈로그, 리뷰, 상품 문의                                                  | `Product`, `Review`, `BQna`, `FileResource`         |
| `cart`     | 장바구니 (복합키)                                                          | `Cart`, `CartId`                                    |
| `order`    | 주문, 주문 항목, 트랜잭션 재고 차감                                               | `Buy`, `OrderItem`, `CQna`                          |
| `wishlist` | 찜 목록 (복합키)                                                          | `Wishlist`, `WishlistId`                            |
| `search`   | Elasticsearch 검색, 인덱스 동기화, 검색 로그                                    | `ProductDocument` (ES), `SearchLog` (MySQL), `ProductSearchRepository`, `ProductIndexService`, `InitialIndexLoader`, `ProductEventListener` |
| `fitting`  | AI 가상 피팅 (Hugging Face Python 브릿지)                                  | `VirtualFitting`, `FittingStatus`, `HuggingFaceGradioService`, `VirtualFittingService` |

### 레이어 구조 (도메인별 5-패키지)
각 도메인 내부는 다음 5개 패키지로 고정한다. (Loopers의 4-Layer Clean Architecture보다 얕고 단순한 구조)

```
com.lookfit.{domain}/
├── domain/         # JPA Entity, Value Object, Enum
├── repository/     # Repository 인터페이스 (+ 필요 시 Impl)
├── service/        # 도메인 비즈니스 로직, @Transactional
├── dto/            # Request/Response DTO (또는 record)
└── controller/     # REST 엔드포인트 (@RestController)
```

### 의존성 규칙
- `{domain}` → `global` ✅
- `{domain}` → 다른 `{domain}` ❌ (직접 import 금지)
- 도메인 간 참조가 필요하면 **ID 기반 호출**로 분리한다. (예: `ReviewService`는 `Buy` 엔티티를 import하지 않고 `BuyRepository`에서 `productId + memberId`로 조회)
- `controller` → `service` → `repository` → `domain` (위에서 아래로만)
- `domain` 패키지는 외부 레이어에 의존하지 않는다 (DIP)

---

## 프론트엔드 구조

```
frontend/src/
├── api/
│   └── axiosInstance.ts      # 싱글톤 Axios; 인터셉터에서 JWT 자동 부착, 401 시 로그아웃 처리
├── context/
│   └── AuthContext.tsx       # 로그인 상태, localStorage 토큰
├── components/               # 페이지 + 재사용 컴포넌트 혼합
│   ├── Header.tsx            # 모바일 햄버거 메뉴
│   ├── LoginSuccess.tsx      # OAuth2 콜백 토큰 파싱
│   ├── VirtualFitting.tsx    # AI 피팅 플로우 (AVIF→JPEG 변환 포함)
│   ├── StarRating.tsx        # 별점 컴포넌트 (읽기/입력 모드)
│   ├── ReviewForm.tsx
│   └── ReviewList.tsx
├── types/                    # 백엔드 DTO 미러 (camelCase)
│   ├── product.ts
│   ├── cart.ts
│   ├── review.ts
│   └── wishlist.ts
├── utils/
│   └── imageUtils.ts         # getImageUrl(): 상대 경로 → 절대 URL
└── styles/
    ├── global.css            # 모바일 우선, iOS 줌 방지 (input ≥16px)
    ├── buttons.css           # 통합 버튼 시스템 (44px 터치 타겟)
    └── Header.css
```

---

## 도메인 & 객체 설계 전략 (SOLID)

LookFit은 DDD 바운디드 컨텍스트를 채택하고 있다. 코드 품질은 다음 원칙을 절대 기준으로 한다.

### SRP — Single Responsibility
- 엔티티는 하나의 일관된 상태 + 그 상태에 직접 관련된 행위만 가진다.
- Service는 **단일 유스케이스 중심**으로 분리한다. 한 클래스가 조회·쓰기·외부 호출을 모두 가지면 분리 후보.
- Controller는 HTTP 바인딩·검증·응답 매핑만 한다. **비즈니스 로직은 Service로**.

### OCP — Open/Closed
- `ErrorCode`는 enum이므로 새 에러는 **추가**만 하고 기존 항목은 변경 금지.
- AI 피팅 공급자 전환이 발생하면 `HuggingFaceGradioService`를 수정하지 말고 공통 인터페이스(예: `VirtualFittingProvider`)를 뽑아서 신규 구현체를 추가한다.
- 정렬/필터/검색 전략은 Strategy 패턴으로 분리해 Service 본문 `if-else` 체인을 막는다.

### LSP — Liskov Substitution
- Repository 인터페이스 구현체는 규약(null 반환 정책, 예외 타입, 페이지 동작)을 위배하지 않는다.
- `Optional<T>`을 반환하는 메서드는 구현체에서 `null`을 던지면 안 된다.

### ISP — Interface Segregation
- Repository 인터페이스는 쓰는 쪽에 필요한 메서드만 노출한다. (Spring Data JPA의 광범위한 `JpaRepository`를 Service가 직접 쥐고 흔드는 것은 ISP 위반으로 본다)
- 신규 도메인은 가능한 한 **도메인 패키지 안에 Repository 인터페이스**를 정의하고, `infrastructure` 역할의 구현체에서 `JpaRepository`를 어댑트한다. (현재 코드베이스는 이 분리가 덜 되어 있으므로 신규 코드부터 적용)

### DIP — Dependency Inversion
- `service/`는 **추상(Repository 인터페이스)** 에 의존하고, 구체(JpaRepository)에는 의존하지 않는다.
- `global` 외부의 구체 클래스(예: `HuggingFaceGradioService`)는 Service 레이어가 직접 쥐지 않고 인터페이스(`VirtualFittingProvider`)로 추상화한다.

### 빈혈 도메인 지양
- 신규 엔티티에는 `@Setter`를 붙이지 않는다.
- 상태 변경은 의미를 가진 엔티티 메서드로만 표현한다. (예: `review.softDelete()`, `cart.changeQuantity(n)`, `order.decreaseStock()`)
- 기존 엔티티(`Review`, `Cart` 등)에 남아 있는 `@Setter`는 **점진적으로 제거한다**. 신규 필드를 추가할 때는 절대 `@Setter`에 기대지 말 것.

### Value Object 도입 가이드
현재 코드베이스는 `memberId`, `productId`를 raw `String`으로 쓰고 있다. 새 도메인을 추가하거나 검증 규칙이 붙는 필드에는 record 기반 VO를 도입한다.

```java
public record ProductId(String value) {
    private static final Pattern PATTERN = Pattern.compile("^[A-Z0-9-]{1,30}$");
    public ProductId {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "productId가 비어 있습니다");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "productId 형식이 올바르지 않습니다");
        }
    }
}
```

---

## 아키텍처 전략 — Layered Architecture + DIP

```
Controller ──┐
             ├──▶  Service  ──▶  Repository (Interface)  ◀── Repository (Impl / JpaRepository)
DTO ─────────┘                            │
                                          ▼
                                        Domain
```

- 의존성 방향은 항상 **바깥 → 안쪽**이다: `Controller → Service → Repository Interface → Domain`.
- `Domain` 패키지는 Spring, JPA 어노테이션을 제외한 어떤 외부 의존도 가져서는 안 된다.
- Controller는 도메인 엔티티를 직접 JSON으로 반환하지 않는다. **반드시 DTO로 변환**한다.

---

## 트랜잭션 & 동시성 규칙

### @Transactional 경계
- 단일 도메인 저장/변경은 **Service 메서드에** `@Transactional`.
- 조회 전용 메서드는 **반드시** `@Transactional(readOnly = true)`.
- 여러 도메인이 얽힌 유스케이스(예: 주문 생성 = 재고 차감 + 결제 + 장바구니 비우기)는 **Facade 레이어 도입을 검토**한다. (현재 코드에는 Facade가 없으므로 신규 도입 시 사용자에게 확인)
- **트랜잭션 내부에 외부 API/프로세스 호출이 있으면 즉시 분리 검토**.
  - 🚨 `HuggingFaceGradioService`의 `ProcessBuilder` 호출은 **트랜잭션 바깥**이어야 한다. Python 프로세스는 수 초~수십 초가 걸리므로 트랜잭션 안에서 실행하면 DB 커넥션이 말라버린다.
  - `VirtualFittingService.generateFitting()`은 상태 저장과 외부 호출을 분리하고, 호출 결과는 비동기로 상태만 업데이트하는 구조를 유지한다.

### Lock 적용 기준
LookFit의 락 후보 엔티티와 권장 전략:

| 엔티티            | 충돌 빈도 | 권장 전략                              |
|-----------------|------|-----------------------------------|
| `Product.stock` | **높음** (주문 동시성) | `PESSIMISTIC_WRITE` (비관 락)        |
| `Buy` (주문)     | 낮음 (1회성) | 락 불필요                               |
| `Review`        | 낮음 (1인 1리뷰 DB unique)  | `@Version` (낙관적 락)                 |
| `Cart` 수량 변경  | 낮음 (세션당 1명) | 락 불필요 (단일 사용자 기준)                |
| `VirtualFitting` 상태 변경 | 낮음 | 락 불필요 (단일 사용자 기준)                |
| `ProductDocument` (ES) | — | ES는 비동기 사용, MySQL이 source of truth |

- 여러 테이블 락을 동시에 잡을 때는 **전역 ID 오름차순**으로 순서를 통일해 데드락을 방지한다.

### 재고 차감 원칙 (주문 도메인)
- `OrderService.createOrder()`는 반드시 비관 락으로 `Product`를 조회한 뒤 차감한다.
- 차감 실패(재고 부족) 시 `BusinessException(ErrorCode.INSUFFICIENT_STOCK)`을 던지고 **전체 주문을 롤백**한다.
- 부분 성공(일부 상품만 예약) 금지.

### Flush & Rollback 점검
- 모든 도메인 예외는 **`BusinessException(unchecked RuntimeException)` 단일 진입점**을 사용한다 → Spring 기본 롤백 대상.
- checked exception을 던지는 서비스 메서드에는 `@Transactional(rollbackFor = Exception.class)`를 **붙이지 말고**, 대신 `BusinessException`으로 감싸서 재던진다.
- `@Transient` 필드는 DB 미반영이 의도된 것인지 주석이나 코드 근거로 드러낸다.

### @Builder.Default 주의 (⚠️ 재발 방지)
**과거 사고 사례**: `Member.enrolldate`에 `@Builder.Default`를 붙였지만 `@Builder`로 생성할 때 조건에 따라 `null`로 저장되어 OAuth2 로그인이 500 에러로 깨졌다. 같은 패턴이 `product/domain/Review.java:43-45` (`createdAt`)에도 아직 남아 있다.

**규칙**: 타임스탬프/감사(audit) 필드는 `@Builder.Default`에 기대지 말고,
1. 생성 시점에 명시적으로 `.createdAt(LocalDateTime.now())`를 전달하거나
2. `@PrePersist` 콜백에서 설정하거나
3. `BaseEntity`(아직 없음 — **도입 권장**) 의 공통 감사 필드로 처리한다.

> 💡 향후 개선: 공통 `BaseEntity` 클래스를 `global/common` 패키지에 만들어 `createdAt/updatedAt/deletedAt`을 `@PrePersist`/`@PreUpdate`로 자동 설정하도록 리팩터링. 도입 전에는 `@Builder.Default` 의존 코드를 리뷰에서 모두 지적한다.

---

## 코드 컨벤션

### 네이밍 규칙 (레이어 간 이중 표기)

**이 규칙은 LookFit에서 가장 자주 충돌 나는 부분이다. 에이전트용 요약본은 `.claude/rules/naming_rules.md`에도 있다.**

| Layer        | Convention | Example                                  |
|--------------|------------|-----------------------------------------|
| DB 컬럼/테이블   | snake_case | `@Column(name = "product_id")`           |
| Java 필드/메서드 | camelCase  | `private String productId;`              |
| JSON (API)   | camelCase  | `@JsonProperty("productId")`             |
| TypeScript   | camelCase  | `productId: string`                      |

**Legacy 예외 (절대 전파 금지)**:
- 기존 `Member` 엔티티: `memberid` (언더스코어 없음) — 구 컨벤션. 신규 필드에 전파 금지.
- 기존 `Product` 엔티티: `pID` 등의 혼용 — 구 컨벤션. 신규 엔티티에는 `product_id` 사용.
- 레거시를 건드릴 때는 스코프가 커지지 않는 한 **이름을 바꾸지 말고 그대로 둔다**. 대신 새 코드에는 새 규칙을 적용한다.

### 클래스/인터페이스 네이밍
- **Entity**: `{Domain}` — `Member`, `Product`, `Review`
- **Service**: `{Domain}Service` — `ProductService`, `ReviewService`
- **Repository 인터페이스**: `{Domain}Repository` — `ReviewRepository`
- **JPA 쿼리 전용**: `{Domain}JpaRepository` (신규 ISP 적용 시)
- **Controller**: `{Domain}V{version}Controller` — `ProductV1Controller`, `ReviewController` (레거시)
  - 신규 컨트롤러는 **반드시 버전 접미사 포함** (`V1`, `V2`).
- **DTO**: `{Domain}Dto` 또는 도메인별 inner record — `ReviewDto.CreateRequest`, `ReviewDto.Response`
- **Value Object**: `{Name}` — `ProductId`, `Email` (신규만)
- **Exception**: `BusinessException` (도메인 예외 단일 진입점)
- **Enum**: `{Name}` — `FittingStatus`, `ErrorCode`

### 메서드 네이밍
- **조회**: `get{Entity}By{Condition}`, `find{Entity}By{Condition}`
- **저장**: `save`, `create`, `register`
- **수정**: `update`, `change{Attribute}`
- **삭제**: `delete` (hard), `softDelete` (tombstone)
- **존재 확인**: `existsBy{Condition}`
- **검증**: `validate{Target}`, `ensure{Condition}`

### 상수 네이밍
- `UPPER_SNAKE_CASE` — `DEFAULT_PAGE_SIZE`, `PASSWORD_PATTERN`

### 타입 사용 규칙

#### Entity
- **타입**: `class` (JPA Entity)
- **생성자**: `protected` 기본 생성자 + 도메인 생성자
- **필드**: `private final` 원칙, 상태 변경은 메서드로만 허용
- **`@Setter` 금지** (신규 엔티티)
- **`@Builder.Default`는 타임스탬프에 금지** (위 사고 사례 참조)

#### DTO
- **권장**: Java `record` (불변, 값 동등성)
- **검증**: `jakarta.validation` 어노테이션 (`@NotBlank`, `@NotNull`, `@Size`, `@Min`)
- **변환**: 엔티티 → DTO는 DTO 내부 **정적 팩토리 메서드** `from(Entity e)`에서 수행
  ```java
  public record Response(Long reviewId, Integer rating, String content, String authorMasked, boolean isOwner) {
      public static Response from(Review review, String viewerMemberId) {
          return new Response(
              review.getReviewId(),
              review.getRating(),
              review.getContent(),
              mask(review.getMemberId()),
              review.getMemberId().equals(viewerMemberId)
          );
      }
  }
  ```

#### Value Object (신규만)
- **타입**: `record`
- **검증**: Compact Constructor에서 수행, 실패 시 `BusinessException(ErrorCode.INVALID_INPUT, ...)`
- **불변성**: record가 자동으로 보장

### 예외 처리

#### BusinessException + ErrorCode
LookFit은 **모든 도메인 예외를 `BusinessException` 하나로 통일**한다. 구분은 `ErrorCode` enum으로 한다.

```java
throw new BusinessException(ErrorCode.REVIEW_NOT_PURCHASED);
throw new BusinessException(ErrorCode.INVALID_INPUT, "별점은 1~5 사이여야 합니다");
```

#### ErrorCode enum
- 위치: `com.lookfit.global.exception.ErrorCode`
- 필드: `HttpStatus status`, `String code`, `String message`
- **신규 에러는 enum에 항목 추가만** (OCP). 기존 항목 변경 시 `code` 값은 절대 바꾸지 말 것 (클라이언트 의존성).

#### GlobalExceptionHandler
- 위치: `com.lookfit.global.exception.GlobalExceptionHandler`
- 처리 대상:
  - `BusinessException` → `ErrorCode`에서 HTTP 상태 추출
  - `MethodArgumentNotValidException` → `400 + INVALID_INPUT`
  - `HttpMessageNotReadableException` → `400 + INVALID_INPUT`
  - `AuthenticationException`, `AccessDeniedException` → `401 / 403`
  - `Exception` (fallback) → `500 + INTERNAL_ERROR`
- Controller에서 `try-catch`로 `BusinessException`을 잡아서 반환 값으로 바꾸지 말 것. 반드시 핸들러까지 흘려보낸다.

### API 응답 구조
현재 LookFit은 표준 래퍼 `ApiResponse<T>`가 **없다**. 신규 엔드포인트는 다음 중 하나를 일관되게 유지한다:

- **성공**: 도메인 DTO를 바로 반환 (`200 OK` + body)
- **실패**: `GlobalExceptionHandler`가 반환하는 `ErrorResponse` 형식
  ```json
  {
    "code": "REVIEW_NOT_PURCHASED",
    "message": "구매한 상품만 리뷰를 작성할 수 있습니다",
    "status": 403,
    "timestamp": "2026-04-10T14:23:11"
  }
  ```

> 💡 공통 `ApiResponse<T>` 래퍼 도입은 가치가 있지만 기존 클라이언트(React 컴포넌트 전체)가 바뀌는 대규모 변경이다. 도입하려면 **반드시 사용자에게 먼저 제안**할 것.

### JPA 규칙
- **Entity**: `@Entity` + `@Table(name = "snake_case")`
- **컬럼**: `@Column(name = "snake_case")` 명시 (implicit naming 의존 금지)
- **연관관계**: **`LAZY` 기본**. `EAGER`는 금지에 가깝고, 꼭 필요하면 사유 주석 필수.
- **`cascade`**: `ALL`과 `REMOVE`는 금지. 필요하면 `PERSIST`, `MERGE`만 명시적으로.
- **N+1 대응**: `@EntityGraph` 또는 QueryDSL `fetchJoin()`으로 해결. `FetchType.EAGER`로 도망치지 말 것.
- **Soft Delete**: `deletedAt` 타임스탬프 컬럼 + `deletedAt IS NULL` 조건을 쿼리에 포함. (LookFit은 `@SQLDelete`/`@Where` 대신 명시적 조건을 선호)

### 의존성 주입
- **생성자 주입만** 사용한다 (필드 주입 금지).
- Lombok `@RequiredArgsConstructor` + `private final` 필드.
- `@Autowired`를 명시적으로 쓰지 않는다.

### 트랜잭션
- Service 레이어의 **public 메서드에만** `@Transactional`을 붙인다.
- 조회 메서드는 `@Transactional(readOnly = true)`.
- Controller·Repository·Entity에는 `@Transactional`을 **붙이지 않는다**.

---

## 테스트 전략

### 테스트 피라미드

| 레벨 | 대상 | 환경 | 목적 |
|------|------|------|------|
| **Unit** | VO, 엔티티 행위, 순수 도메인 로직 | Spring 없이 순수 JVM | 규칙·상태 전이 검증 |
| **Integration** | Service, Repository | `@SpringBootTest` (`@DataJpaTest`도 가능) | 비즈니스 흐름 + DB 검증 |
| **E2E (백엔드)** | REST API 전체 흐름 | `@SpringBootTest(webEnvironment=RANDOM_PORT)` + `TestRestTemplate` | HTTP 요청/응답 시나리오 |
| **E2E (프론트엔드)** | 사용자 여정 | **Puppeteer** (`frontend/e2e-*.cjs`) | 실제 브라우저에서의 UI 플로우 |

### 단위 테스트
- **명명**: `{ClassName}Test` 또는 `{ClassName}UnitTest`
- **패턴**: 3A (Arrange – Act – Assert)
- **어노테이션**: `@DisplayName` (한글 의도 표현 필수), `@Nested` (케이스 그룹화)
- **라이브러리**: JUnit 5, AssertJ (`assertThat`), Mockito
- **예시**:
```java
@DisplayName("리뷰 작성 시,")
@Nested
class WriteReview {
    @DisplayName("구매하지 않은 상품이면 REVIEW_NOT_PURCHASED 예외가 발생한다.")
    @Test
    void throwsWhenNotPurchased() {
        // arrange
        given(buyRepository.existsByMemberIdAndProductId("u1", "p1")).willReturn(false);

        // act
        BusinessException ex = catchThrowableOfType(
            () -> reviewService.create("u1", "p1", new CreateRequest(5, "good")),
            BusinessException.class);

        // assert
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_PURCHASED);
    }
}
```

### 통합 테스트
- **명명**: `{ClassName}IntegrationTest`
- **어노테이션**: `@SpringBootTest` 또는 `@DataJpaTest` (+ `@Import`)
- **DB**: `h2` (현재 `testRuntimeOnly`로 잡혀 있음). TestContainers 도입은 Loopers처럼 후속 과제.
- **격리**: 각 테스트 메서드는 `@Transactional` 롤백 또는 명시적 정리.

### E2E — Puppeteer 스크립트
- 위치: `frontend/e2e-*.cjs`
- **전제**: 백엔드 `:8080` + 프론트엔드 `:5173` 수동 실행 후 `node e2e-review-flow.cjs`
- 실행 단위가 느슨한 브라우저 테스트이므로, 기능 추가 시 **기존 스크립트를 지우지 말고** 해당 기능용 새 `.cjs`를 추가한다.
- E2E 실패 시 screen shot을 `frontend/` 아래 임시 파일로 떨어뜨려 확인.

### 테스트 원칙 (절대 규칙)
1. **Mock 최소화**: 가능하면 실제 DB/실제 HTTP를 쓴다. Mock은 외부 유료 API(`HuggingFaceGradioService`의 Python 호출)에만.
2. **assertion 약화 금지**: 실패하는 assertion을 맞추기 위해 `assertThat(x).isNotNull()`로 내리는 행위 금지.
3. **`@Disabled` 금지**: 불안정한 테스트는 고치거나 삭제하되, 그 판단은 사용자에게 먼저 알린다.
4. **테스트 이름은 한국어 `@DisplayName` + 영어 메서드명**.

---

## 크리티컬 아키텍처 시임 (LookFit 고유)

### 1. OAuth2 → JWT → Axios 인터셉터 체인
이 체인은 **4개 파일이 얽혀 있고**, 과거 버그(500 에러, `enrolldate` null, Authorization 헤더 누락)가 모두 여기서 났다. 하나를 건드리면 전체를 추적하라.

```
[사용자] ─ GET /oauth2/authorization/google
     └──▶ Spring Security OAuth2 Client
              └──▶ OAuth2SuccessHandler.onAuthenticationSuccess()
                        ├── Member 생성/조회 (⚠️ enrolldate 명시 설정)
                        ├── JwtTokenProvider.createToken(memberId)
                        └── redirect: {FRONTEND_URL}/login/success?token=...
                                       │
[브라우저] ────────────────────────────┘
     └──▶ LoginSuccess.tsx
              └── localStorage.setItem('token', ...)
                  └── navigate('/')
                         │
[이후 요청] ─────────────┘
     └──▶ axiosInstance.ts (interceptor)
              ├── request: headers.Authorization = `Bearer ${token}`
              └── response 401: localStorage.removeItem, redirect /login
                         │
[백엔드] ────────────────┘
     └──▶ JwtAuthenticationFilter (SecurityFilterChain)
              └── validate → SecurityContextHolder.setAuthentication
```

**수정 시 체크리스트**:
- [ ] `OAuth2SuccessHandler.java`에서 Member 생성할 때 `enrolldate`가 `.build()` 전에 명시적으로 설정되었는가?
- [ ] 리다이렉트 URL에 토큰이 정상적으로 들어가는가?
- [ ] `axiosInstance.ts`의 요청 인터셉터가 **모든** 엔드포인트에 적용되는가? (새 컴포넌트가 `fetch()`로 요청하고 있지는 않은가?)
- [ ] `JwtAuthenticationFilter`가 `UsernamePasswordAuthenticationFilter` **앞**에 등록되어 있는가?
- [ ] `SecurityConfig`가 401을 리다이렉트로 처리하지 않고 JSON 응답으로 반환하는가?

### 2. Elasticsearch 인덱스 생명주기
**MySQL이 source of truth, ES는 파생 뷰**. 이 구분을 절대 섞지 말 것.

```
앱 기동 (ApplicationReadyEvent)
  └──▶ InitialIndexLoader.run()
         └── ProductIndexService.reindexAll()
                └── MySQL Product 전체 → ProductDocument → ES bulk index

엔티티 변경 (save/update/delete)
  └──▶ ProductEventListener (@PostPersist / @PostUpdate / @PostRemove)
         └── ProductIndexService.indexSingle(productId) (@Async)

관리자 재인덱싱
  └──▶ POST /api/v1/admin/search/reindex
         └── ProductIndexService.reindexAll()
```

**⚠️ 주의사항**:
- `bootRun` 시 **Elasticsearch가 반드시 켜져 있어야 한다**. `InitialIndexLoader`는 `ApplicationReadyEvent`에서 실행되며, ES 연결 실패 시 앱 기동이 거칠게 실패한다. `docker-compose up -d`로 항상 올려두고 테스트 시작할 것.
- `ProductDocument`의 매핑 또는 `product-settings.json`(Nori analyzer 설정)을 바꾸면 Spring Data Elasticsearch는 **자동으로 reindex 해 주지 않는다**. 수동으로 `DELETE /products` 후 재기동 필요.
- `@Async` 경로의 예외는 호출자에게 전파되지 않으므로 **로그로만 추적 가능**. 인덱스가 MySQL과 어긋난 것 같으면 `POST /api/v1/admin/search/reindex`로 전체 재인덱싱.

### 3. Hugging Face Python 브릿지 (AI 피팅)
AI 피팅은 **Java에서만 읽으면 이해 불가능**하다. Python 스크립트와 양방향으로 오가는 JSON 프로토콜이 존재한다.

```
FittingController.generate
  └── VirtualFittingService.generateFitting
         ├── upload 파일 저장 (local FS, S3 아님)
         ├── VirtualFitting(status=PROCESSING) 저장
         └── HuggingFaceGradioService.call(userImagePath, garmentImagePath, category)
                └── ProcessBuilder: python3 backend/scripts/virtual_tryon.py ...
                        │
                        ▼
                  [Python 프로세스]
                  gradio_client → yisol/IDM-VTON Hugging Face Space
                  └── stdout에 JSON 한 줄 출력
                       │
                       ▼
                  Java: stdout에서 JSON 파싱
                  ├── 성공: result 이미지 경로 저장, status=COMPLETED
                  └── error_type=QUOTA_EXCEEDED → BusinessException(GPU_QUOTA_EXCEEDED)
```

**치명 규칙**:
- `virtual_tryon.py`에 **`print()`를 추가하면 Java side의 JSON 파싱이 깨진다**. Gradio 자체 stdout 로그는 스크립트 내에서 suppress 처리되어 있다. 디버깅이 필요하면 `sys.stderr`에 찍어라. Java 쪽은 stdout만 읽는다.
- 환경변수 `HF_TOKEN`이 설정되어 있지 않으면 Python이 Hugging Face 인증에서 실패한다. `application.yml`의 `.env` 로딩과 함께 확인.
- 이 호출은 **수 초~수십 초**가 걸린다. Service 메서드를 `@Transactional`로 감싸지 말 것 (커넥션 풀 고갈).
- 업로드/결과 이미지는 로컬 파일 시스템 (`fitting.upload-dir`, `fitting.result-dir`)에 저장된다. S3 마이그레이션은 **미완**이며, 현재 배포판은 EC2 로컬 디스크에 의존한다.
- 프론트엔드 쪽: `VirtualFitting.tsx`에서 **AVIF → JPEG 변환을 Canvas API로** 수행한다 (Python 측이 AVIF를 읽지 못함). 새 이미지 포맷을 지원하려면 이 변환 로직도 함께 고쳐야 한다.

---

## 개발 규칙

### 속도보다 통제
- AI는 코드의 의도, 변경 영향, 책임에 대한 컨텍스트를 지속적으로 유지할 수 없다.
- **개발자가 의도를 정의하고, AI는 승인된 범위 안에서만 구현**한다.
- 제안은 얼마든지 할 수 있지만, **최종 설계 결정은 사용자에게 위임**한다.

### Claude 역할 제한
| 허용                         | 금지                              |
|-----------------------------|----------------------------------|
| 제안, 대안 제시, 트레이드오프 설명   | 임의 설계 결정, 요구 범위 확장 |
| 승인된 범위 내 구현              | 테스트 삭제, `@Disabled`, assertion 약화 |
| 테스트 작성 및 실행               | 동작 변경이 포함된 "리팩터링 겸 기능 추가" |
| 승인 후 리팩터링                 | 사용하지 않는 코드의 투기적 선삭제           |
| 사용자 확인 후 새 의존성 추가     | 라이센스·보안·유지보수 확인 없이 라이브러리 추가   |

### TDD Workflow (Red → Green → Refactor)

> 테스트는 구현 검증이 아니라 **설계 단위 검증**이다.

#### 🔴 Red — 실패하는 테스트 먼저
- 요구사항을 테스트 케이스로 고정.
- **컴파일 에러가 아니라 assertion 실패**로 Red여야 한다.
- 프로덕션 코드는 최소 껍데기만 생성. 이 단계에서 로직 쓰지 말 것.

#### 🟢 Green — 테스트만 통과시키는 최소 코드
- Red의 테스트 **딱** 통과하는 선까지만 구현.
- 오버 엔지니어링 금지. 미래 요구 예측 금지.
- 기존 테스트도 모두 통과해야 한다.

#### 🔵 Refactor — 동작 변경 없이 품질 개선
- 중복 제거, 네이밍 개선, 불필요 import 제거.
- 새 기능 추가 금지. 새 기능이 필요하면 Red부터 다시.
- 전 테스트 슈트 통과해야 완료.

---

## 사용자 강제 규칙 — 테스트 없이 "완료" 선언 금지 🚨

**이것은 사용자의 명시적·반복적 요구사항이며, 협상 불가능하다.**

백엔드/프론트엔드 변경을 한 뒤 **실제로 돌려보지 않고** "완료"라고 말하지 말 것. 판정 기준은:

1. `./gradlew test` — 단위·통합 테스트 전부 통과
2. `./gradlew bootRun` — 서버가 "Started LookFitApplication" 로그까지 정상 기동
3. 변경한 엔드포인트를 `curl` / `httpie`로 직접 호출하거나, 변경한 UI를 브라우저 또는 Puppeteer 스크립트로 한 번 돌려본다
4. 하나라도 실패하면 즉시 수정 후 재검증

컴파일 성공만 확인하고 "완료" 보고하면 사용자가 직접 다시 돌려서 실패를 발견하는 패턴이 재발한다. 과거 이 규칙을 건너뛰어서 생긴 실제 사고:
- OAuth2 `enrolldate` null → 로그인 500
- `ReviewForm`에 Authorization 헤더 누락 → 401
- JWT 필터 등록 순서 오류 → 모든 인증 우회
- 한글 인코딩 `?` 표시 → MySQL connection charset

---

## 절대 금지사항 (Never Do)

### ❌ 실제 동작하지 않는 코드
- Mock 데이터로만 동작하는 구현, 가짜 응답 반환 금지.
- 화면에 "데모"라고만 보이고 실제로 DB/API를 타지 않는 코드 금지.

### ❌ null-safety 위반
- `Optional<T>` 반환 메서드에서 `null` 반환 금지.
- 외부 입력은 **경계 계층(Controller, OAuth2SuccessHandler, 파일 업로드)에서 검증**한다.
- JPA 연관관계(`@ManyToOne`)가 null이 될 수 있으면 `optional = true`를 명시한다.

### ❌ `System.out.println` / `printStackTrace`
- 디버깅 로그는 `@Slf4j` + SLF4J 사용.
- `e.printStackTrace()`는 금지. `log.error("...", e)`로 대체.

### ❌ 테스트 임의 변경
- 실패 테스트를 지우거나 `@Disabled` 달지 말 것.
- assertion 약화 금지.
- 테스트 코드의 동작이 바뀌면 그 이유가 **코멘트나 PR 설명으로 설명 가능**해야 한다.

### ❌ 레거시 네이밍 전파
- `memberid`, `pID` 같은 legacy 컬럼명을 **새 엔티티에 복사 붙여넣지 말 것**.
- 기존 엔티티에 새 필드를 추가할 때는 새 규칙(`member_id`, `product_id`)을 적용한다.

### ❌ 트랜잭션 안 외부 호출
- `@Transactional` 내부에서 `HuggingFaceGradioService.call`, 외부 HTTP, Kafka publish 금지.
- 필요하면 트랜잭션을 나누거나, `TransactionalEventListener(phase=AFTER_COMMIT)`으로 분리.

---

## 권장사항 (Recommendation)

### ✅ E2E 테스트로 실제 HTTP 왕복 검증
- `TestRestTemplate` 기반 백엔드 E2E
- Puppeteer 기반 프론트엔드 E2E
- 새 API 추가 시 최소 1개의 E2E 시나리오 함께 추가

### ✅ 재사용 가능한 객체 설계
- Value Object(record) 적극 활용
- 정적 팩토리 메서드 `from()`, `of()` 제공
- 불변 객체 우선

### ✅ 성능 고려 및 제안
- N+1 문제 → `@EntityGraph`, QueryDSL `fetchJoin()`
- 인덱스 필요 시 PR에서 명시적으로 제안
- 캐싱(Redis)은 도입 전 반드시 사용자 확인 (현재 `build.gradle.kts`에 Redis 의존성은 주석 처리되어 있음)

### ✅ 개발 완료된 API는 수동 테스트 스크립트 저장
- `backend/` 또는 `scripts/` 아래에 `.http` 또는 `.sh`로 curl 시나리오 저장
- 새 환경에서 곧바로 재현 가능하도록

### ✅ 신규 도메인 도입 시 Repository 인터페이스 분리
- Domain package에 `interface XxxRepository` 선언
- Infrastructure(또는 `repository` 패키지의 Impl 클래스)에서 `JpaRepository` 어댑트
- Service는 인터페이스에만 의존 (DIP)

---

## 우선순위 (Priority)

1. **실제 동작하는 해결책**이 이론적 우아함보다 우선.
2. **null-safety, 동시성**을 설계 단계에서 고려. 사후 땜질 금지.
3. **테스트 가능한 구조**로 설계. 생성자 주입, 인터페이스 분리, 시간·랜덤 등 비결정 요소는 추상화.
4. **기존 코드 패턴과의 일관성**. 더 나은 패턴이 있더라도 스코프 바깥에서는 손대지 말고 제안만.

---

## 도메인별 핵심 규칙

### Member
- 소셜 로그인 단일 경로 (Google OAuth2). 자체 회원가입 API는 **없다**.
- `enrolldate`는 `OAuth2SuccessHandler`에서 **명시적으로** 설정한다 (⚠️ `@Builder.Default` 버그 재발 방지).
- 회원 조회 시 `deletedAt IS NULL` 조건을 반드시 포함.

### Product
- 상품 목록은 **페이징 필수** (무한 스크롤도 `Pageable`로 구현).
- 재고 변경은 `@Transactional` + 비관 락.
- 상품 변경 시 `ProductEventListener`가 ES 인덱스를 `@Async`로 갱신한다. **MySQL 저장을 성공시킨 후** 이벤트를 발생시킬 것.

### Cart
- 동일 상품 재추가는 **수량 증가**로 처리 (새 row 추가 금지).
- 재고 초과 시 `BusinessException(PRODUCT_OUT_OF_STOCK)`.
- `Cart.imageUrl`은 `Product`에서 읽어 저장된 스냅샷. 상품 이미지 변경과 동기화하지 않는다.

### Order
- 주문 생성은 **단일 트랜잭션**: 재고 차감 + 주문 저장 + 장바구니 비우기가 모두 원자적.
- 재고 부족 시 **전체 주문 롤백** (부분 성공 금지).
- 주문 조회는 `@Transactional(readOnly=true)`.

### Review
- **구매 확인 필수** (`Buy + OrderItem`에 해당 `productId + memberId` 존재).
- 1인 1상품 1리뷰 (`deletedAt IS NULL` 조건 포함).
- 삭제는 soft delete (`deletedAt` 타임스탬프).
- 작성자 표시는 마스킹 (`user***`).
- 수정/삭제는 본인만 가능 (`review.getMemberId().equals(viewerMemberId)`).
- 이미지 업로드: 로컬 FS, 최대 5MB, `jpg/jpeg/png/webp`.

### Wishlist
- 복합키 (`member_id + product_id`).
- 토글 방식 또는 명시적 추가/삭제 — **한 도메인 내에서 일관성** 유지.

### Search
- ES는 MySQL의 파생 뷰. 검색 결과에서 가격·재고 등 volatile 필드는 **최종 응답 시 MySQL에서 다시 확인하는 것**을 원칙으로 한다. (그래야 인덱스 지연에 덜 민감)
- 인기 검색어는 `SearchLog`(MySQL)에서 집계. ES에 직접 로그 저장 금지.
- 관리자 재인덱싱 API (`POST /api/v1/admin/search/reindex`)는 ADMIN 권한만.

### Fitting
- 절대 `@Transactional` 안에서 Python 프로세스 호출 금지.
- 업로드 이미지와 결과 이미지는 로컬 FS. 영속 저장이 필요하면 별도 백업 경로 필요.
- `QUOTA_EXCEEDED` 에러는 **사용자에게 보이는 메시지**로 변환해 안내 (Hugging Face 무료 플랜 GPU 할당량 이슈).

---

## 환경 설정

### 프로파일
| 프로파일   | 용도                              |
|-----------|---------------------------------|
| `default` | 로컬 개발 (application.yml)          |
| `prod`    | 운영 (application-prod.yml)        |

### 인프라 실행
```bash
# MySQL + Elasticsearch 로컬 컨테이너
docker-compose up -d
docker-compose logs -f elasticsearch   # Nori 플러그인 로드 확인
```

### 필수 환경변수
`.env` 또는 `application.yml`에:
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` — OAuth2
- `JWT_SECRET` — JWT 서명 키
- `HF_TOKEN` — Hugging Face API 토큰
- `spring.datasource.url/username/password` — MySQL 접속
- `elasticsearch.uris` — 기본 `http://localhost:9200`

---

## 실행 커맨드

### 백엔드 (`backend/`)
```bash
./gradlew bootRun                                                         # 개발 서버 (:8080)
./gradlew build                                                           # 컴파일 + 테스트 + jar
./gradlew clean build                                                     # QueryDSL Q-class 재생성 (엔티티 이름 변경 후 필수)
./gradlew test                                                            # 전체 테스트
./gradlew test --tests "com.lookfit.product.service.ReviewServiceTest"    # 단일 클래스
./gradlew test --tests "*ReviewServiceTest.shouldRejectDuplicateReview"   # 단일 메서드
```

> **QueryDSL 주의**: Q-class는 `backend/src/main/generated/`에 생성된다. `clean` 시 이 디렉토리가 삭제되므로, 엔티티 이름을 바꾼 뒤 `cannot find symbol: QProduct` 오류가 나면 `./gradlew clean build`.

### 프론트엔드 (`frontend/`)
```bash
npm run dev                        # Vite dev server (:5173)
npm run build                      # tsc -b && vite build
npm run lint                       # ESLint
node e2e-review-flow.cjs           # 단일 E2E (백엔드·프론트엔드 모두 기동 상태여야 함)
node e2e-mobile-quick-test.cjs     # 모바일 반응형
```

> `npm test`는 없다. 테스트는 루트의 `e2e-*.cjs` 독립 스크립트.

### 모듈별 빌드
```bash
./gradlew :backend:build           # 백엔드만
./gradlew jacocoTestReport         # 커버리지 리포트 (설정되어 있다면)
```

---

## 모바일 반응형 규칙 (프론트엔드 작업 시)

LookFit은 **모바일 우선(Mobile-First)** 설계다. 프론트엔드 수정 시 아래 규칙을 위반하지 않도록 한다.

### 브레이크포인트
| 카테고리 | 너비 | 주요 레이아웃 |
|-----------|------|---------------|
| Small Mobile | ≤ 480px | 1열, 액션 버튼 숨김 (hover 불가) |
| Mobile | 481~768px | 2열 (ProductList), 햄버거 메뉴 |
| Tablet | 769~1023px | 3열 (ProductList), 헤더 검색창 풀폭 |
| Desktop | ≥ 1024px | 4열 (ProductList), 가로 네비 |

CSS 변수:
```css
--breakpoint-sm: 640px;
--breakpoint-md: 768px;
--breakpoint-lg: 1024px;
--breakpoint-xl: 1280px;
```

### 터치 타겟 — **최소 44×44px** (Apple HIG / Material Design)
- 버튼·링크·체크박스·라디오 모두 적용
- 버튼 사이즈 시스템: `sm` 36px, default 44px, `lg` 52px, `xl` 60px
- 신규 버튼은 반드시 `frontend/src/styles/buttons.css`의 `.btn`, `.btn-primary` 등의 유틸 클래스를 사용 (임시 inline style 금지)

### iOS 줌 방지 (필수)
```css
input[type="text"], input[type="email"], input[type="password"], textarea, select {
  font-size: max(16px, var(--text-base));  /* iOS는 16px 미만 input 포커스 시 자동 줌인 */
}
```

### 터치 최적화
```css
button, a, [role="button"] {
  touch-action: manipulation;            /* 더블탭 줌 방지 */
  -webkit-tap-highlight-color: transparent;
}

html {
  -webkit-text-size-adjust: 100%;        /* 가로모드 자동 확대 방지 */
}
```

### 햄버거 메뉴 체크리스트 (`Header.tsx` 수정 시)
- [ ] `useState`로 `mobileMenuOpen` 상태 관리
- [ ] 라우트 변경 시 자동으로 메뉴 닫힘 (`useEffect(..., [location])`)
- [ ] 오버레이 클릭 시 메뉴 닫힘
- [ ] 메뉴 오픈 시 `body.style.overflow = 'hidden'` (스크롤 잠금)
- [ ] `aria-expanded`, `aria-label="menu"` 속성 필수

### 반응형 디바이스 테스트 최소 셋
- iPhone SE (375px) — 최소 폭
- iPhone 12/13/14 (390px) — 대표 모바일
- iPad Mini (768px) — 태블릿 경계
- iPad Pro (1024px) — 데스크톱 경계
- Desktop 1280px+ — 기본 4열

### 절대 하지 말 것
- ❌ 고정 px 폭 레이아웃 (`width: 1200px` 등) → `max-width` + `%` 사용
- ❌ `hover:` 상태만으로 기능 제공 (터치 디바이스는 hover 없음)
- ❌ 버튼 터치 타겟 < 44px
- ❌ `input` 폰트 크기 < 16px
- ❌ 모바일에서 가로 스크롤 발생

---

## 참고 문서

- **README.md** — 프로젝트 소개, 전체 API 엔드포인트 목록, 배포 퀵스타트
- **TROUBLESHOOTING.md** — 재발 방지 이슈 8건 (OAuth2 500, 인코딩, 401, ES, 리뷰, E2E, OAuth redirect URI)
- **DEPLOYMENT_GUIDE.md** — AWS EC2/GitHub Actions 파이프라인 + 대안 배포(Vercel/Cloudflare/환경변수/OAuth 상세)
- **docs/PRD.md** — 제품 요구사항 정의서 (FR/NFR, 페르소나, KPI, Open Questions)
- **docs/AI_FITTING_PLAN_HUGGINGFACE.md** — 가상 피팅 Hugging Face 연동 설계 문서
- **.claude/rules/naming_rules.md** — 에이전트용 네이밍 규칙 요약 (CLAUDE.md 「코드 컨벤션」 축약판)

---

## 알려진 기술 부채

- **`BaseEntity` 미도입** — 현재 각 엔티티가 `createdAt/updatedAt/deletedAt`을 개별 관리. `@Builder.Default` 기반 초기화가 여러 곳에 남아 재발 위험.
- **`@Setter` 잔존** — 기존 엔티티가 `@Setter`로 상태 변경 가능. 빈혈 도메인 모델.
- **`google-genai` 의존성 잔재** — `build.gradle.kts:73`, 사용되지 않음.
- **`build.gradle.kts` 끝 부분 중복 블록** — `group`, `java`, `configurations` 블록이 중복 선언되어 있음 (line 114-128). 기능상 문제 없음.
- **S3 미마이그레이션** — 업로드·피팅 결과 이미지가 로컬 FS. 운영 배포 스케일 아웃 시 장애 요인.
- **ApiResponse 래퍼 부재** — 응답 포맷이 엔드포인트마다 다를 수 있음. 통일 필요.
- **Repository 인터페이스/구현 분리 미완** — 현재 대부분 Service가 `JpaRepository`를 직접 의존. ISP/DIP 개선 여지.

신규 기능 구현 시 위 항목을 손대야 한다면 **반드시 사용자에게 스코프 확장 여부를 먼저 확인**할 것.
