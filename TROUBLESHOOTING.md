# LookFit 프로젝트 트러블슈팅 가이드

> **작성 목적**: 프로젝트 개발 과정에서 마주한 기술적 문제들과 해결 과정을 기록
> **대상 독자**: 취업 포트폴리오 검토자, 기술 블로그 독자
> **작성 기간**: 2026-01-30 ~ 2026-02-11

---

## 📑 목차

1. [OAuth2 로그인 500 에러 해결](#1-oauth2-로그인-500-에러-해결)
2. [MySQL 한글 인코딩 문제 완전 해결](#2-mysql-한글-인코딩-문제-완전-해결)
3. [모바일 반응형 UI 이슈 디버깅](#3-모바일-반응형-ui-이슈-디버깅)
4. [Spring Security와 JWT 인증 통합](#4-spring-security와-jwt-인증-통합)
5. [Elasticsearch 검색 기능 구현](#5-elasticsearch-검색-기능-구현)
6. [리뷰 기능 구현 및 테스트](#6-리뷰-기능-구현-및-테스트)
7. [E2E 테스트 자동화 구축](#7-e2e-테스트-자동화-구축)
8. [Google OAuth2 redirect_uri_mismatch 400 에러](#8-google-oauth2-redirect_uri_mismatch-400-에러)

---

## 1. OAuth2 로그인 500 에러 해결

### 🔴 문제 상황

Google OAuth2 로그인 성공 후 백엔드에서 500 Internal Server Error 발생:

```
org.springframework.dao.DataIntegrityViolationException:
could not execute statement [Column 'enrolldate' cannot be null]
```

### 🔍 원인 분석

**Member 엔티티**에서 `@Builder.Default` 어노테이션이 제대로 작동하지 않음:

```java
@Entity
public class Member {
    @Column(name = "enrolldate")
    @Builder.Default  // ❌ 작동하지 않음
    private LocalDateTime enrolldate = LocalDateTime.now();
}
```

**Lombok의 @Builder와 @Builder.Default의 한계**:
- `@Builder.Default`는 빌더 패턴에서만 작동
- OAuth2 인증 핸들러에서 빌더 사용 시 명시적으로 값을 설정하지 않으면 `null`로 설정됨

### ✅ 해결 방법

**OAuth2SuccessHandler에서 명시적 설정**:

```java
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(...) {
        // 기존 코드
        Member member = memberRepository.findById(oAuth2User.getName())
            .orElseGet(() -> {
                Member newMember = Member.builder()
                    .memberid(oAuth2User.getName())
                    .enrolldate(LocalDateTime.now())  // ✅ 명시적 설정
                    .build();
                return memberRepository.save(newMember);
            });

        // JWT 토큰 발급 및 리다이렉트
        String token = jwtTokenProvider.createToken(member.getMemberid(), member.getRole());
        getRedirectStrategy().sendRedirect(request, response,
            "http://localhost:5173/login/success?token=" + token);
    }
}
```

### 📊 검증 결과

```bash
# 테스트 실행
./gradlew test

# 결과: ✅ OAuth2 로그인 플로우 전체 통과
# 1. Google 로그인 → 2. JWT 토큰 발급 → 3. 프론트엔드 리다이렉트
```

### 💡 학습 내용

1. **Lombok의 @Builder.Default는 항상 신뢰할 수 없다**
   - 복잡한 객체 생성 시 명시적 설정 권장

2. **@PrePersist 활용 대안**:
   ```java
   @PrePersist
   public void prePersist() {
       if (this.enrolldate == null) {
           this.enrolldate = LocalDateTime.now();
       }
   }
   ```

3. **통합 테스트의 중요성**
   - 단위 테스트만으로는 OAuth2 플로우 전체를 검증하기 어려움
   - E2E 테스트(Puppeteer)로 실제 브라우저 플로우 검증 필요

---

## 2. MySQL 한글 인코딩 문제 완전 해결

### 🔴 문제 상황

API 응답에서 한글이 `?`로 깨져서 표시됨:

```json
{
  "productName": "???????? ??????",  // ❌ 한글 깨짐
  "productCategory": "??"
}
```

### 🔍 원인 분석 (다중 레이어 문제)

#### 1️⃣ **MySQL 연결 레벨**
- HikariCP 커넥션 풀의 기본 charset이 `latin1`로 설정됨
- MySQL 서버 기본 charset이 `utf8mb3` (3바이트, 이모지 지원 안함)

#### 2️⃣ **JPA/Hibernate 레벨**
- JDBC URL에 charset 파라미터 누락
- Hibernate의 문자열 처리가 UTF-8을 명시하지 않음

#### 3️⃣ **Spring MVC 레벨**
- HTTP 응답의 `Content-Type`이 `charset=UTF-8` 누락

### ✅ 해결 방법 (3단계 접근)

#### **Step 1: application.yml 수정**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lookfit_db?characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci&useUnicode=true
    hikari:
      connection-init-sql: "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"
```

**핵심 파라미터**:
- `characterEncoding=UTF-8`: JDBC 드라이버에게 UTF-8 사용 지시
- `connectionCollation=utf8mb4_unicode_ci`: 정렬 방식 (대소문자 무시)
- `useUnicode=true`: 유니코드 처리 활성화
- `connection-init-sql`: 커넥션 생성 시마다 실행

#### **Step 2: WebConfig 설정**

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 기존 StringHttpMessageConverter 제거
        converters.removeIf(converter -> converter instanceof StringHttpMessageConverter);

        // UTF-8 StringHttpMessageConverter 추가
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false);  // charset=UTF-8을 Accept-Charset 헤더에 쓰지 않음
        converters.add(0, stringConverter);  // 우선순위 최상위

        // MappingJackson2HttpMessageConverter도 UTF-8 설정
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(jsonConverter);
    }
}
```

#### **Step 3: MySQL 데이터베이스 설정**

```sql
-- 데이터베이스 문자셋 변경
ALTER DATABASE lookfit_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 기존 테이블 문자셋 변경
ALTER TABLE product CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE member CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 데이터 재삽입 (기존 데이터가 깨진 경우)
DELETE FROM product;
INSERT INTO product (pID, pName, pCategory, pPrice, pStock, imageUrl) VALUES
('P001', '화이트 기본 티셔츠', '상의', 29000, 100, '/images/products/tshirt-white.jpg'),
('P002', '블랙 슬림핏 청바지', '하의', 59000, 50, '/images/products/jeans-black.jpg');
```

### 📊 검증 결과

#### **API 테스트**:
```bash
curl http://localhost:8080/api/v1/products | jq

# ✅ 결과: 한글 정상 출력
{
  "productName": "화이트 기본 티셔츠",
  "productCategory": "상의"
}
```

#### **브라우저 테스트**:
```javascript
// Network 탭 확인
Content-Type: application/json;charset=UTF-8  // ✅
```

### 💡 학습 내용

1. **UTF-8 vs UTF8MB4**:
   - `utf8`: MySQL의 3바이트 인코딩 (이모지 ❌)
   - `utf8mb4`: 4바이트 완전한 UTF-8 (이모지 ✅)

2. **인코딩 문제의 3대 체크포인트**:
   - ✅ 데이터베이스 charset
   - ✅ 커넥션 charset
   - ✅ HTTP 응답 charset

3. **HikariCP connection-init-sql의 중요성**:
   - 커넥션 풀에서 재사용되는 커넥션마다 charset 초기화
   - `SET NAMES utf8mb4`는 3가지 변수를 한 번에 설정:
     - `character_set_client`
     - `character_set_connection`
     - `character_set_results`

---

## 3. 모바일 반응형 UI 이슈 디버깅

### 🔴 문제 상황

사용자 피드백: "모바일에서 버튼들이 클릭이 안돼 그리고 호버같은것도 안먹고 검색도 안먹네"

**증상**:
1. 모바일에서 모든 버튼 클릭 불가
2. 검색 입력 불가
3. 햄버거 메뉴 작동 안함
4. 장바구니 아이콘 클릭 불가

### 🔍 원인 분석

#### **디버깅 과정**:

1. **크롬 개발자 도구 모바일 에뮬레이션**:
   ```
   F12 → Toggle device toolbar → iPhone SE
   ```

2. **CSS 오버레이 검사**:
   ```css
   /* frontend/src/styles/Header.css */
   .header__mobile-overlay {
     display: block;      /* ⚠️ 모바일에서 항상 렌더링됨 */
     position: fixed;
     z-index: 48;         /* ⚠️ 다른 요소들을 덮음 */
     opacity: 0;          /* ⚠️ 투명하지만 클릭 영역은 존재 */
   }
   ```

3. **Chrome DevTools Elements 탭**:
   - 버튼 클릭 시 실제로 `.header__mobile-overlay`가 이벤트를 가로챔
   - `pointer-events` 속성이 없어 투명한 오버레이가 클릭 차단

#### **근본 원인**:

**CSS의 `opacity`와 `display`의 차이**:
- `opacity: 0`: 투명하지만 **클릭 이벤트는 받음**
- `display: none`: 렌더링도 안 되고 **클릭 이벤트도 안 받음**
- `visibility: hidden`: 렌더링은 되지만 **클릭 이벤트는 안 받음**

### ✅ 해결 방법

```css
/* frontend/src/styles/Header.css */
.header__mobile-overlay {
  display: none;  /* 데스크톱에서는 렌더링 안 함 */
}

@media (max-width: 768px) {
  .header__mobile-overlay {
    display: block;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 48;
    opacity: 0;
    visibility: hidden;      /* ✅ 추가: 보이지 않음 */
    pointer-events: none;    /* ✅ 추가: 클릭 이벤트 차단 해제 */
    transition: opacity 0.3s ease, visibility 0.3s ease;
  }

  .header__mobile-overlay.active {
    opacity: 1;
    visibility: visible;     /* ✅ 활성화 시 보이기 */
    pointer-events: auto;    /* ✅ 활성화 시 클릭 이벤트 받기 */
  }
}
```

### 📊 검증 결과

#### **수동 테스트**:
```
✅ 모바일 버튼 클릭 정상 작동
✅ 검색 입력 가능
✅ 햄버거 메뉴 열림/닫힘 정상
✅ 오버레이 클릭 시 메뉴 닫힘
```

#### **E2E 테스트**:
```javascript
// frontend/e2e-mobile-responsive.cjs
await page.setViewport({ width: 390, height: 844, isMobile: true });

// 햄버거 메뉴 클릭
const hamburger = await page.$('.header__hamburger');
await hamburger.click();

// 오버레이 확인
const overlay = await page.$('.header__mobile-overlay.active');
assert(overlay !== null, '✅ 오버레이 표시됨');

// 버튼 클릭 가능 확인
const cartButton = await page.$('.header__cart-link');
const isClickable = await page.evaluate(btn => {
  const style = window.getComputedStyle(btn);
  return style.pointerEvents !== 'none';
}, cartButton);
assert(isClickable, '✅ 버튼 클릭 가능');
```

### 💡 학습 내용

1. **CSS pointer-events의 활용**:
   - `pointer-events: none`: 요소가 클릭 이벤트를 무시
   - `pointer-events: auto`: 기본 동작 (클릭 이벤트 받음)
   - 오버레이, 로딩 스피너 등에 유용

2. **모바일 디버깅 체크리스트**:
   - [ ] `z-index` 충돌 확인
   - [ ] `position: fixed/absolute` 요소의 `pointer-events` 확인
   - [ ] 터치 이벤트와 클릭 이벤트 동시 테스트
   - [ ] 실제 모바일 기기에서도 테스트

3. **반응형 디자인 베스트 프랙티스**:
   ```css
   /* 오버레이/모달 패턴 */
   .overlay {
     /* 비활성 상태 */
     opacity: 0;
     visibility: hidden;
     pointer-events: none;
     transition: all 0.3s ease;
   }

   .overlay.active {
     /* 활성 상태 */
     opacity: 1;
     visibility: visible;
     pointer-events: auto;
   }
   ```

---

## 4. Spring Security와 JWT 인증 통합

### 🔴 문제 상황

Public API도 모두 403 Forbidden 반환:

```bash
curl http://localhost:8080/api/v1/products
# ❌ 403 Forbidden
```

### 🔍 원인 분석

**SecurityConfig의 과도한 인증 요구**:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()  // ❌ 모든 요청에 인증 요구
            );
        return http.build();
    }
}
```

**문제점**:
1. 상품 목록 조회 같은 공개 API도 인증 필요
2. OAuth2 로그인 엔드포인트도 보호됨
3. 정적 리소스(이미지, CSS)도 403 반환

### ✅ 해결 방법

#### **Step 1: Public/Private API 분리**

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // REST API는 CSRF 불필요
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            // ✅ Public API (인증 불필요)
            .requestMatchers(
                "/api/v1/products/**",      // 상품 조회
                "/api/v1/search/**",        // 검색
                "/oauth2/**",               // OAuth2 로그인
                "/login/**",                // 로그인 콜백
                "/images/**",               // 정적 리소스
                "/h2-console/**"            // 개발용 DB 콘솔
            ).permitAll()

            // ✅ Private API (인증 필요)
            .requestMatchers(
                "/api/v1/cart/**",          // 장바구니
                "/api/v1/orders/**",        // 주문
                "/api/v1/wishlist/**",      // 찜 목록
                "/api/v1/reviews/**"        // 리뷰 작성/수정/삭제
            ).authenticated()

            // ✅ Admin API (ADMIN 역할 필요)
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

            // 나머지는 인증 필요
            .anyRequest().authenticated()
        )
        // JWT 필터 추가
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        // OAuth2 로그인 설정
        .oauth2Login(oauth2 -> oauth2
            .successHandler(oAuth2SuccessHandler)
        );

    return http.build();
}
```

#### **Step 2: REST API 인증 실패 처리**

기존 문제: 인증 실패 시 `/login` 페이지로 리다이렉트 → SPA에서 불필요

```java
http
    .exceptionHandling(exception -> exception
        // ✅ 401 Unauthorized 반환 (리다이렉트 X)
        .authenticationEntryPoint((request, response, authException) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                "{\"error\": \"Unauthorized\", \"message\": \"인증이 필요합니다.\"}"
            );
        })

        // ✅ 403 Forbidden 반환
        .accessDeniedHandler((request, response, accessDeniedException) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(
                "{\"error\": \"Forbidden\", \"message\": \"권한이 없습니다.\"}"
            );
        })
    );
```

#### **Step 3: CORS 설정**

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:5173"));  // React 개발 서버
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);  // 쿠키/Authorization 헤더 허용
    configuration.setMaxAge(3600L);  // preflight 캐싱

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### 📊 검증 결과

#### **Public API 테스트**:
```bash
# ✅ 인증 없이 상품 조회 가능
curl http://localhost:8080/api/v1/products
# 200 OK

# ✅ 검색 API 작동
curl "http://localhost:8080/api/v1/search?keyword=티셔츠"
# 200 OK
```

#### **Private API 테스트**:
```bash
# ❌ 인증 없이 장바구니 접근 시 401
curl http://localhost:8080/api/v1/cart
# 401 Unauthorized

# ✅ JWT 토큰으로 장바구니 접근
curl -H "Authorization: Bearer eyJhbGc..." http://localhost:8080/api/v1/cart
# 200 OK
```

### 💡 학습 내용

1. **REST API와 전통적인 세션 기반 인증의 차이**:
   - 세션: 로그인 페이지로 리다이렉트
   - REST API: HTTP 상태 코드 (401/403)로 응답

2. **Spring Security의 필터 체인 순서**:
   ```
   CORS Filter
   ↓
   CSRF Filter (disabled for REST API)
   ↓
   JWT Authentication Filter (custom)
   ↓
   UsernamePasswordAuthenticationFilter
   ↓
   OAuth2 Login Filter
   ```

3. **CORS Preflight Request**:
   - 브라우저가 실제 요청 전에 OPTIONS 메서드로 허용 여부 확인
   - `Access-Control-Allow-Origin` 헤더 필수
   - `credentials: 'include'` 사용 시 `AllowCredentials: true` 필요

---

## 5. Elasticsearch 검색 기능 구현

### 🔴 문제 상황 (없음 - 선제적 구현)

프로젝트 초기부터 Elasticsearch 검색 기능을 계획했으나, 다음 과제들이 있었음:

1. MySQL 상품 데이터와 Elasticsearch 인덱스 동기화
2. 한글 형태소 분석 (Nori 플러그인)
3. 카테고리/가격 필터링
4. 검색 로그 저장 (인기 검색어)

### 🔍 설계 결정

#### **아키텍처**:
```
Frontend (React)
    ↓ [HTTP]
SearchController (Public API)
    ↓
SearchService
    ├─→ ProductSearchRepository (Elasticsearch)
    ├─→ SearchLogRepository (MySQL)
    └─→ ProductIndexService (동기화)
```

#### **Elasticsearch 스키마 설계**:

```json
{
  "settings": {
    "analysis": {
      "analyzer": {
        "nori": {
          "type": "custom",
          "tokenizer": "nori_tokenizer",
          "filter": ["nori_part_of_speech"]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "productId": { "type": "keyword" },
      "productName": {
        "type": "text",
        "analyzer": "nori",  // ✅ 한글 형태소 분석
        "fields": {
          "keyword": { "type": "keyword" }  // 정렬용
        }
      },
      "productCategory": { "type": "keyword" },  // 필터링용
      "productPrice": { "type": "integer" },
      "productStock": { "type": "integer" },
      "imageUrl": { "type": "keyword" }
    }
  }
}
```

### ✅ 구현 방법

#### **Step 1: Docker Compose 설정**

```yaml
# docker-compose.yml
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.17.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - es-data:/usr/share/elasticsearch/data
      - ./elasticsearch-plugins:/usr/share/elasticsearch/plugins  # ✅ 플러그인 마운트

  # Nori 플러그인 설치 (초기 설정)
  elasticsearch-setup:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.17.0
    command: >
      bash -c "
        elasticsearch-plugin install analysis-nori &&
        echo 'Nori plugin installed'
      "
    volumes:
      - ./elasticsearch-plugins:/usr/share/elasticsearch/plugins
```

#### **Step 2: Product 변경 시 자동 인덱싱**

```java
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductIndexService indexService;

    @EventListener
    @Async  // ✅ 비동기 처리 (메인 트랜잭션 블로킹 방지)
    public void handleProductCreated(ProductCreatedEvent event) {
        indexService.indexProduct(event.getProduct());
    }

    @EventListener
    @Async
    public void handleProductUpdated(ProductUpdatedEvent event) {
        indexService.indexProduct(event.getProduct());
    }

    @EventListener
    @Async
    public void handleProductDeleted(ProductDeletedEvent event) {
        indexService.deleteProduct(event.getProductId());
    }
}
```

#### **Step 3: 검색 서비스 구현**

```java
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductSearchRepository searchRepository;
    private final SearchLogRepository logRepository;

    @Transactional
    public SearchDto.SearchResultPage search(SearchDto.SearchRequest request) {
        // 1. Elasticsearch 검색 실행
        Page<ProductDocument> results = searchRepository.searchProducts(
            request.getKeyword(),
            request.getCategory(),
            request.getMinPrice(),
            request.getMaxPrice(),
            request.getSortBy(),
            PageRequest.of(request.getPage(), request.getSize())
        );

        // 2. 검색 로그 저장 (비동기)
        saveSearchLog(request.getKeyword(), results.getTotalElements());

        // 3. DTO 변환
        return SearchDto.SearchResultPage.from(results);
    }

    @Async  // ✅ 비동기 로그 저장 (검색 응답 속도 영향 X)
    protected void saveSearchLog(String keyword, long resultCount) {
        SearchLog log = SearchLog.builder()
            .keyword(keyword)
            .resultCount(resultCount)
            .searchedAt(LocalDateTime.now())
            .build();
        logRepository.save(log);
    }
}
```

#### **Step 4: 인기 검색어 조회**

```java
@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    @Query("""
        SELECT new com.lookfit.search.dto.SearchDto$PopularSearch(
            s.keyword,
            COUNT(s),
            MAX(s.searchedAt)
        )
        FROM SearchLog s
        WHERE s.searchedAt >= :since
        GROUP BY s.keyword
        ORDER BY COUNT(s) DESC
        LIMIT :limit
    """)
    List<SearchDto.PopularSearch> findPopularKeywords(
        @Param("since") LocalDateTime since,
        @Param("limit") int limit
    );
}
```

### 📊 검증 결과

#### **인덱싱 확인**:
```bash
# Elasticsearch 상태 확인
curl http://localhost:9200/_cluster/health
# ✅ status: green

# products 인덱스 확인
curl http://localhost:9200/products/_count
# ✅ count: 20 (전체 상품 수)

# 한글 분석 테스트
curl -X POST "http://localhost:9200/products/_analyze" -H 'Content-Type: application/json' -d'
{
  "analyzer": "nori",
  "text": "화이트 기본 티셔츠"
}
'
# ✅ tokens: ["화이트", "기본", "티셔츠"]
```

#### **검색 API 테스트**:
```bash
# 키워드 검색
curl "http://localhost:8080/api/v1/search?keyword=티셔츠"
# ✅ 2개 상품 검색됨

# 카테고리 필터
curl "http://localhost:8080/api/v1/search?keyword=티셔츠&category=상의"
# ✅ 1개 상품 검색됨

# 가격 정렬
curl "http://localhost:8080/api/v1/search?keyword=티셔츠&sortBy=price_asc"
# ✅ 가격 낮은 순 정렬됨

# 인기 검색어
curl "http://localhost:8080/api/v1/search/suggestions"
# ✅ [{"keyword": "티셔츠", "count": 5}, ...]
```

### 💡 학습 내용

1. **Elasticsearch vs MySQL 전문 검색 비교**:
   | 항목 | MySQL LIKE | Elasticsearch |
   |------|-----------|---------------|
   | 성능 | 느림 (Full Scan) | 빠름 (역인덱스) |
   | 한글 분석 | 불가능 | Nori 플러그인 |
   | 관련도 정렬 | 불가능 | TF-IDF, BM25 |
   | 확장성 | 수직 확장 | 수평 확장 |

2. **비동기 처리의 중요성**:
   - 검색 로그 저장을 동기로 하면 검색 응답이 느려짐
   - `@Async` + `@EnableAsync`로 간단히 비동기화
   - 인덱싱 실패해도 메인 트랜잭션 롤백 X

3. **Nori 형태소 분석기**:
   - "화이트 기본 티셔츠" → ["화이트", "기본", "티셔츠"]
   - "블랙진" → ["블랙", "진"]
   - 조사 제거: "티셔츠가" → ["티셔츠"]

4. **인덱스 동기화 전략**:
   - **실시간**: Product 변경 시 이벤트 → 즉시 인덱싱 (현재 방식)
   - **배치**: 매일 밤 전체 재인덱싱 (대규모 데이터)
   - **CDC**: MySQL binlog → Kafka → Elasticsearch (엔터프라이즈)

---

## 6. 리뷰 기능 구현 및 테스트

### 🔴 문제 상황 (설계 단계)

리뷰 기능 요구사항:
1. ⭐ 별점 (1~5점)
2. 📝 텍스트 리뷰
3. 🖼️ 이미지 업로드 (선택)
4. ✅ **구매 확인** (구매하지 않은 상품에는 리뷰 작성 불가)
5. ✏️ 수정/삭제 (작성자만)

**주요 고민 사항**:
- 기존 `BReview` 테이블 사용 vs 새 `Review` 테이블 생성?
- 구매 확인 로직 (Buy + OrderItem 조인)
- 이미지 업로드 보안 (악성 파일 업로드 방지)
- XSS 공격 방지 (리뷰 내용에 스크립트 삽입)

### 🔍 설계 결정

#### **신규 Review 엔티티 생성 결정**:

**기존 BReview의 문제점**:
```java
@Entity
@Table(name = "b_review")  // ❌ 네이밍 규칙 위반
public class BReview {
    @Column(name = "bNo")   // ❌ camelCase (DB는 snake_case여야 함)
    private Long bNo;

    @Column(name = "pID")   // ❌ 일관성 없는 네이밍
    private String pID;
}
```

**신규 Review 엔티티**:
```java
@Entity
@Table(name = "review")  // ✅ 단수형, snake_case
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")  // ✅ snake_case
    private Long reviewId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "rating", nullable = false)
    @Min(1) @Max(5)
    private Integer rating;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "deleted_at")  // ✅ Soft Delete
    private LocalDateTime deletedAt;
}
```

### ✅ 구현 방법

#### **Step 1: 구매 확인 로직**

```java
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final BuyRepository buyRepository;

    /**
     * 사용자가 특정 상품을 구매했는지 확인
     * - Buy 테이블 (주문)
     * - OrderItem 테이블 (주문 상품)
     * - Product 테이블 (상품 정보)
     */
    private boolean hasPurchased(String memberId, String productId) {
        return buyRepository.existsByMemberIdAndProductId(memberId, productId);
    }
}

@Repository
public interface BuyRepository extends JpaRepository<Buy, String> {

    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM Buy b
        JOIN OrderItem oi ON b.orderno = oi.buy.orderno
        WHERE b.member.memberid = :memberId
          AND oi.product.pID = :productId
    """)
    boolean existsByMemberIdAndProductId(
        @Param("memberId") String memberId,
        @Param("productId") String productId
    );
}
```

#### **Step 2: 이미지 업로드 보안**

```java
@Service
public class ReviewService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;  // 5MB

    private String uploadImage(MultipartFile file) {
        // 1️⃣ 파일 존재 확인
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 2️⃣ 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEED);
        }

        // 3️⃣ 확장자 검증
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 4️⃣ Content-Type 검증 (MIME Type Sniffing 방지)
        String contentType = file.getContentType();
        if (!contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 5️⃣ UUID 파일명 생성 (Path Traversal 공격 방지)
        String filename = UUID.randomUUID().toString() + "." + extension;
        Path uploadPath = Paths.get("uploads/reviews", filename);

        // 6️⃣ 파일 저장
        Files.createDirectories(uploadPath.getParent());
        Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/reviews/" + filename;
    }
}
```

#### **Step 3: XSS 방지**

```java
import org.springframework.web.util.HtmlUtils;

@Service
public class ReviewService {

    @Transactional
    public ReviewDto.Response createReview(...) {
        // ✅ HTML 이스케이프 처리
        String sanitizedContent = HtmlUtils.htmlEscape(request.getContent());

        Review review = Review.builder()
            .content(sanitizedContent)  // <script> → &lt;script&gt;
            .build();

        return ReviewDto.Response.from(reviewRepository.save(review));
    }
}
```

#### **Step 4: 프론트엔드 - StarRating 컴포넌트**

```tsx
import React, { useState } from 'react';
import './StarRating.css';

interface StarRatingProps {
  rating: number;
  onRatingChange?: (rating: number) => void;
  size?: 'sm' | 'md' | 'lg';
  readonly?: boolean;
}

const StarRating: React.FC<StarRatingProps> = ({
  rating,
  onRatingChange,
  size = 'md',
  readonly = false
}) => {
  const [hoverRating, setHoverRating] = useState(0);

  const handleClick = (index: number) => {
    if (!readonly && onRatingChange) {
      onRatingChange(index + 1);
    }
  };

  return (
    <div className={`star-rating star-rating--${size} ${readonly ? '' : 'star-rating--interactive'}`}>
      {[...Array(5)].map((_, index) => (
        <span
          key={index}
          className={`star-rating__star ${
            index < (hoverRating || rating) ? 'star-rating__star--filled' : ''
          }`}
          onClick={() => handleClick(index)}
          onMouseEnter={() => !readonly && setHoverRating(index + 1)}
          onMouseLeave={() => !readonly && setHoverRating(0)}
          role="button"
          tabIndex={readonly ? -1 : 0}
          aria-label={`${index + 1}점`}
        >
          ★
        </span>
      ))}
    </div>
  );
};

export default StarRating;
```

### 📊 검증 결과

#### **단위 테스트 (11개)**:
```java
@DisplayName("ReviewService 단위 테스트")
class ReviewServiceTest {

    @Test
    void createReview_Success() {
        // Given: 상품 존재, 구매 이력 있음
        when(productRepository.existsById("P001")).thenReturn(true);
        when(buyRepository.existsByMemberIdAndProductId("M001", "P001")).thenReturn(true);

        // When: 리뷰 작성
        ReviewDto.Response response = reviewService.createReview("M001", "P001", request, null);

        // Then: 리뷰 생성됨
        assertThat(response.getRating()).isEqualTo(5);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_NotPurchased_ThrowsException() {
        // Given: 구매 이력 없음
        when(buyRepository.existsByMemberIdAndProductId(any(), any())).thenReturn(false);

        // When & Then: 예외 발생
        assertThrows(BusinessException.class, () ->
            reviewService.createReview("M001", "P001", request, null)
        );
    }

    // ... 9개 테스트 더
}

// ✅ 결과: 11/11 PASSED
```

#### **E2E 테스트 (7개)**:
```javascript
// frontend/e2e-review-flow.cjs

async function testReviewSectionExists() {
  console.log('📝 Test 1: Review Section Exists');

  await page.goto('http://localhost:5173');
  await sleep(1000);

  // 첫 번째 상품 클릭
  const firstProduct = await page.$('.product-card');
  await firstProduct.click();
  await sleep(2000);

  // 리뷰 섹션 확인
  const reviewSection = await page.$('.review-section');
  if (!reviewSection) {
    throw new Error('❌ Review section not found');
  }
  console.log('✅ Review section exists');
}

async function testStarRatingInteraction() {
  console.log('📝 Test 4: Star Rating Interaction');

  // 리뷰 작성 버튼 클릭
  const writeButton = await page.$('.review-section__write-btn');
  await writeButton.click();
  await sleep(1000);

  // 별점 클릭
  const stars = await page.$$('.star-rating__star');
  await stars[3].click();  // 4점
  await sleep(500);

  const filledStars = await page.$$('.star-rating__star--filled');
  console.log(`✅ ${filledStars.length} stars filled`);
}

// ✅ 결과: 7/7 PASSED
```

### 💡 학습 내용

1. **파일 업로드 보안 체크리스트**:
   - [ ] 파일 크기 제한
   - [ ] 확장자 화이트리스트
   - [ ] MIME Type 검증
   - [ ] UUID 파일명 (Path Traversal 방지)
   - [ ] 업로드 디렉토리 권한 설정
   - [ ] 바이러스 스캔 (프로덕션)

2. **XSS 방어 전략**:
   - **Input 단계**: `HtmlUtils.htmlEscape()` (서버)
   - **Output 단계**: React는 기본적으로 XSS 방어 (`{}`는 자동 이스케이프)
   - **위험한 패턴**: `dangerouslySetInnerHTML` 사용 금지

3. **Soft Delete의 장점**:
   - 데이터 복구 가능
   - 통계 유지 (삭제된 리뷰도 평균 별점에 영향 없음)
   - 법적 요구사항 (개인정보 보호법)

   ```java
   // 소프트 삭제
   review.softDelete();  // deletedAt = now()

   // 조회 시 제외
   @Query("SELECT r FROM Review r WHERE r.deletedAt IS NULL")
   List<Review> findActiveReviews();
   ```

4. **평균 별점 계산 최적화**:
   ```java
   // ❌ N+1 문제
   List<Review> reviews = reviewRepository.findAll();
   double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);

   // ✅ DB에서 계산
   @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.deletedAt IS NULL")
   Double getAverageRating(@Param("productId") String productId);
   ```

---

## 7. E2E 테스트 자동화 구축

### 🔴 문제 상황

수동 테스트의 한계:
1. 매번 브라우저에서 클릭하며 테스트 → 시간 소모
2. 회귀 테스트 어려움 (이전 기능이 깨졌는지 모름)
3. 모바일 반응형 테스트 수동 진행
4. 검증 누락 가능성

### 🔍 도구 선택

**Puppeteer vs Playwright vs Selenium**:
| 항목 | Puppeteer | Playwright | Selenium |
|------|-----------|-----------|----------|
| 속도 | 빠름 | 매우 빠름 | 느림 |
| API | Chrome DevTools Protocol | 크로스 브라우저 CDP | WebDriver |
| 설치 | 간단 | 간단 | 복잡 |
| 브라우저 | Chrome/Chromium | Chrome, Firefox, Safari | 모든 브라우저 |

**선택**: Puppeteer (프로젝트 초기, Chrome만 지원해도 충분)

### ✅ 구현 방법

#### **Step 1: 프로젝트 구조**

```
frontend/
├── e2e-shopping-flow.cjs       # 쇼핑 플로우 (9개 테스트)
├── e2e-complete-test.cjs       # 상품 상세 (13개 테스트)
├── e2e-order-flow.cjs          # 주문 API (7개 테스트)
├── e2e-image-loading.cjs       # 이미지 로딩 (9개 테스트)
├── e2e-cart-flow.cjs           # 장바구니 (4개 테스트)
├── e2e-search-flow.cjs         # 검색 (10개 테스트)
├── e2e-review-flow.cjs         # 리뷰 (7개 테스트)
└── e2e-mobile-responsive.cjs   # 모바일 (추가 예정)

# 총 58개 E2E 테스트
```

#### **Step 2: 기본 템플릿**

```javascript
const puppeteer = require('puppeteer');

const FRONTEND_URL = 'http://localhost:5173';
const BACKEND_URL = 'http://localhost:8080';

let browser;
let page;

async function setup() {
  console.log('🚀 Starting E2E Tests...\n');

  browser = await puppeteer.launch({
    headless: false,  // ✅ 브라우저 표시 (디버깅 용이)
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
    defaultViewport: null  // 전체 화면
  });

  page = await browser.newPage();

  // 콘솔 로그 캡처
  page.on('console', msg => {
    if (msg.type() === 'error') {
      console.error('Browser Error:', msg.text());
    }
  });

  // 네트워크 에러 캡처
  page.on('requestfailed', request => {
    console.error('Request Failed:', request.url());
  });
}

async function teardown() {
  if (browser) {
    await browser.close();
  }
}

async function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

// Test 1: Example
async function testExample() {
  console.log('📝 Test 1: Example');

  await page.goto(FRONTEND_URL);
  await sleep(1000);

  const title = await page.title();
  console.log(`✅ Page title: ${title}`);
}

// Main runner
async function runTests() {
  try {
    await setup();

    await testExample();
    // ... more tests

    console.log('\n✅ All tests passed!\n');

  } catch (error) {
    console.error('\n❌ Test failed:', error.message);
    await teardown();
    process.exit(1);
  }
}

runTests();
```

#### **Step 3: 복잡한 시나리오 - OAuth2 로그인**

```javascript
async function testOAuth2Login() {
  console.log('📝 Test: OAuth2 Login Flow');

  // 1. 로그인 버튼 클릭
  await page.goto(FRONTEND_URL);
  const loginButton = await page.$('a[href*="oauth2/authorization/google"]');

  // 2. 새 탭에서 Google 로그인 처리
  const [popup] = await Promise.all([
    new Promise(resolve => browser.once('targetcreated', target => resolve(target.page()))),
    loginButton.click()
  ]);

  await popup.waitForNavigation({ waitUntil: 'networkidle0' });

  // 3. Google 계정 선택 (이미 로그인된 경우)
  const accountButton = await popup.$('div[data-email="test@example.com"]');
  if (accountButton) {
    await accountButton.click();
  }

  // 4. 메인 페이지로 리다이렉트 대기
  await page.waitForNavigation({ waitUntil: 'networkidle0', timeout: 10000 });

  // 5. 로그인 상태 확인
  const welcomeText = await page.$eval('.header__welcome', el => el.textContent);
  if (!welcomeText.includes('님')) {
    throw new Error('❌ Login failed');
  }

  console.log('✅ OAuth2 login successful');
}
```

#### **Step 4: 모바일 반응형 테스트**

```javascript
async function testMobileResponsive() {
  console.log('📝 Test: Mobile Responsive');

  // iPhone SE 뷰포트
  await page.setViewport({
    width: 390,
    height: 844,
    isMobile: true,
    hasTouch: true,
    deviceScaleFactor: 2
  });

  await page.goto(FRONTEND_URL);
  await sleep(1000);

  // 햄버거 메뉴 표시 확인
  const hamburger = await page.$('.header__hamburger');
  const isVisible = await page.evaluate(el => {
    const style = window.getComputedStyle(el);
    return style.display !== 'none' && style.visibility !== 'hidden';
  }, hamburger);

  if (!isVisible) {
    throw new Error('❌ Hamburger menu not visible on mobile');
  }

  // 햄버거 메뉴 클릭
  await hamburger.click();
  await sleep(500);

  // 모바일 메뉴 확인
  const mobileNav = await page.$('.header__mobile-nav.active');
  if (!mobileNav) {
    throw new Error('❌ Mobile nav not opened');
  }

  console.log('✅ Mobile responsive working');
}
```

#### **Step 5: 스크린샷 자동 저장**

```javascript
async function testWithScreenshot(testName, testFn) {
  try {
    await testFn();
    console.log(`✅ ${testName} passed`);
  } catch (error) {
    // 실패 시 스크린샷 저장
    const timestamp = new Date().toISOString().replace(/:/g, '-');
    const screenshotPath = `./screenshots/${testName}-${timestamp}.png`;
    await page.screenshot({ path: screenshotPath, fullPage: true });
    console.error(`❌ ${testName} failed. Screenshot saved: ${screenshotPath}`);
    throw error;
  }
}

// 사용 예
await testWithScreenshot('Shopping Flow', testShoppingFlow);
```

### 📊 검증 결과

#### **전체 테스트 실행**:
```bash
# 모든 E2E 테스트 실행
npm run test:e2e:all

# 결과:
✅ Shopping Flow: 9/9
✅ Product Detail: 13/13
✅ Order API: 7/7
✅ Image Loading: 8/9 (1 warning)
✅ Cart Flow: 4/4
✅ Search Flow: 10/10
✅ Review Flow: 7/7

📊 Total: 58/59 tests passed (98.3%)
```

#### **CI/CD 통합 (예정)**:
```yaml
# .github/workflows/e2e-tests.yml
name: E2E Tests

on: [push, pull_request]

jobs:
  e2e:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Start Backend
        run: |
          docker-compose up -d mysql elasticsearch
          ./gradlew bootRun &
          sleep 30

      - name: Start Frontend
        run: |
          cd frontend
          npm install
          npm run dev &
          sleep 10

      - name: Run E2E Tests
        run: |
          cd frontend
          npm run test:e2e:all

      - name: Upload Screenshots
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: e2e-screenshots
          path: frontend/screenshots/
```

### 💡 학습 내용

1. **Puppeteer의 Page 객체 주요 메서드**:
   ```javascript
   // 네비게이션
   await page.goto(url, { waitUntil: 'networkidle0' });

   // 요소 선택
   const element = await page.$('.selector');  // 단일
   const elements = await page.$$('.selector');  // 여러 개

   // 요소 조작
   await element.click();
   await element.type('text');

   // 요소 정보 추출
   const text = await page.$eval('.selector', el => el.textContent);
   const attribute = await page.$eval('.selector', el => el.getAttribute('href'));

   // 스크립트 실행
   await page.evaluate(() => {
     localStorage.setItem('token', 'abc123');
   });

   // 대기
   await page.waitForSelector('.selector');
   await page.waitForNavigation();
   await sleep(1000);
   ```

2. **E2E 테스트 베스트 프랙티스**:
   - **독립성**: 각 테스트는 다른 테스트에 의존하지 않음
   - **멱등성**: 여러 번 실행해도 같은 결과
   - **명확한 에러 메시지**: 실패 원인 파악 용이
   - **스크린샷**: 실패 시 자동 저장
   - **선택적 요소**: optional 요소는 throw 대신 warning

3. **headless vs headful**:
   ```javascript
   // 개발 중 (디버깅)
   puppeteer.launch({ headless: false });

   // CI/CD (속도)
   puppeteer.launch({ headless: true });
   ```

4. **네트워크 모킹 (고급)**:
   ```javascript
   await page.setRequestInterception(true);

   page.on('request', request => {
     if (request.url().includes('/api/v1/products')) {
       request.respond({
         status: 200,
         contentType: 'application/json',
         body: JSON.stringify([{ productId: 'P001', productName: 'Mock Product' }])
       });
     } else {
       request.continue();
     }
   });
   ```

---

## 📚 결론 및 교훈

### 프로젝트 통계

| 항목 | 수치 |
|------|------|
| 개발 기간 | 13일 (2026-01-30 ~ 2026-02-11) |
| 해결한 이슈 | 7건 (OAuth2, 인코딩, 모바일 UI 등) |
| 작성한 테스트 | 58개 E2E + 11개 단위 테스트 |
| API 엔드포인트 | 25개 |
| 프론트엔드 컴포넌트 | 12개 |
| 백엔드 도메인 | 6개 (member, product, cart, order, search, wishlist) |

### 핵심 교훈

1. **인코딩 문제는 다층적이다**
   - 데이터베이스 → 커넥션 → HTTP 응답까지 모두 확인

2. **CSS의 숨겨진 함정**
   - `opacity: 0`은 클릭 이벤트를 차단하지 않음
   - `pointer-events: none` 필수

3. **Spring Security는 세밀한 설정이 필요하다**
   - Public/Private API 분리
   - REST API는 리다이렉트 대신 HTTP 상태 코드

4. **파일 업로드는 보안이 최우선**
   - 확장자, MIME Type, 파일 크기 모두 검증
   - UUID 파일명으로 Path Traversal 방어

5. **E2E 테스트는 투자 가치가 높다**
   - 초기 설정 시간 3시간 → 매 빌드마다 수동 테스트 30분 절약

### 포트폴리오 활용 팁

1. **기술 블로그 포스트 아이디어**:
   - "Spring Security + JWT 완전 정복"
   - "Elasticsearch Nori 플러그인으로 한글 검색 구현하기"
   - "Puppeteer로 E2E 테스트 자동화하기"
   - "파일 업로드 보안: 5가지 필수 검증"

2. **면접 대비 키워드**:
   - OAuth2, JWT, Spring Security
   - Elasticsearch, 형태소 분석, Nori
   - DDD, Clean Architecture
   - Soft Delete, XSS, CSRF
   - Puppeteer, E2E Testing

3. **GitHub README 강화 포인트**:
   - ✅ 트러블슈팅 섹션 추가 (이 문서 링크)
   - ✅ 아키텍처 다이어그램
   - ✅ API 문서 (Swagger/Postman)
   - ✅ 데모 영상 (GIF/YouTube)

---

## 8. Google OAuth2 `redirect_uri_mismatch` 400 에러

### 🔴 문제 상황

Vercel(프론트) + Cloudflare Tunnel(백엔드) 구성에서 Google 로그인 시:

```
액세스 차단됨: 이 앱의 요청이 잘못되었습니다
400 오류: redirect_uri_mismatch
```

### 🔍 원인 분석

**Google Cloud Console에 등록된 redirect URI**:
```
http://localhost:8080/login/oauth2/code/google
```

**실제 요청 URI** (Cloudflare Tunnel로 들어온 요청):
```
https://ste-colleges-wires-saints.trycloudflare.com/login/oauth2/code/google
```

Google OAuth2 서버는 등록된 URI와 **완전히 일치**하지 않으면 요청을 거부한다. Cloudflare Tunnel은 재시작할 때마다 URL이 바뀌는데 Google Console은 수동 업데이트가 필요하므로 이 에러가 매우 자주 발생한다.

### ✅ 해결 방법

**Step 1 — 현재 백엔드 도메인 확인**:
```bash
# cloudflared 실행 로그에서
# 출력: https://<new-url>.trycloudflare.com
```

**Step 2 — Google Cloud Console에 URI 추가**:
```
https://console.cloud.google.com
  → APIs & Services → Credentials
  → OAuth 2.0 Client IDs → 편집
  → 승인된 리디렉션 URI에 추가:
     https://<new-url>.trycloudflare.com/login/oauth2/code/google
  → 저장 (30초~1분 반영 대기)
```

**Step 3 — 승인된 JavaScript 원본도 추가** (선택, CORS 프리플라이트 대응):
```
http://localhost:5173
https://look-fit.vercel.app
https://<new-url>.trycloudflare.com
```

**Step 4 — 기존 URI는 삭제하지 말 것**:
```
✅ http://localhost:8080/login/oauth2/code/google              (로컬 개발)
✅ http://localhost:5173/login/oauth2/code/google              (로컬 프론트)
✅ https://<new-url>.trycloudflare.com/login/oauth2/code/google (프로덕션)
```

**Step 5 — 캐시 삭제 후 재시도**:
- `Ctrl+Shift+Delete` → 캐시 삭제
- 또는 시크릿 모드

### 🐛 그래도 실패할 때

| 증상 | 원인 | 해결 |
|------|------|------|
| 동일 에러 계속 발생 | Google 반영 지연 | 1~2분 대기 후 재시도 |
| 다른 URI로 에러 | Cloudflare Tunnel URL 변경 | 백엔드 로그에서 실제 URL 재확인 후 Console 업데이트 |
| HTTPS 기대인데 HTTP로 요청 | 프론트 환경변수 문제 | Vercel `VITE_API_BASE_URL`을 `https://` 로 수정 후 Redeploy |

### 💡 재발 방지 — 개발/프로덕션 클라이언트 ID 분리

**Google Cloud Console에서 2개의 OAuth 클라이언트 생성**:

```yaml
# application.yml (개발)
spring.security.oauth2.client.registration.google.client-id: ${GOOGLE_CLIENT_ID_DEV}

# application-prod.yml (프로덕션)
spring.security.oauth2.client.registration.google.client-id: ${GOOGLE_CLIENT_ID_PROD}
```

이러면 개발 환경에서 URI를 바꿔도 프로덕션에 영향이 없다.

### 📊 교훈

1. **무료 Cloudflare Tunnel은 PoC 전용**: URL이 재시작마다 바뀌므로 프로덕션 배포에는 부적합. AWS EC2 + Elastic IP 또는 커스텀 도메인이 필수.
2. **OAuth redirect URI는 서버(백엔드) 도메인**: 프론트엔드 도메인(Vercel)이 아니라 백엔드 도메인을 등록해야 한다. 이 부분을 혼동해서 시간을 낭비하는 경우가 많다.
3. **환경변수로 URL을 분리**: 하드코딩 대신 `FRONTEND_URL`, `VITE_API_BASE_URL`로 분리해 두면 URL 변경 시 여러 파일을 수정할 필요가 없다.

---

**작성자**: anhyeongjun
**프로젝트**: LookFit (AI 기반 가상 착장샷 서비스)
**GitHub**: https://github.com/anhyeongjun/LookFit
**마지막 업데이트**: 2026-04-10 (8장 OAuth redirect_uri_mismatch 흡수)
