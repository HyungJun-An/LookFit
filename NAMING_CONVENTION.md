# LookFit 네이밍 컨벤션 (Naming Convention)

> **작성일**: 2026-02-02
> **목적**: 데이터베이스, 백엔드, 프론트엔드 간 일관된 네이밍 규칙으로 혼란 방지

---

## 📌 핵심 원칙

### 1. **Database (MySQL)** - snake_case
### 2. **Backend (Java)** - camelCase
### 3. **Frontend (TypeScript)** - camelCase
### 4. **JSON API** - camelCase

---

## 1️⃣ Database (MySQL) 네이밍 규칙

### 테이블명
- **형식**: `snake_case` (소문자 + 언더스코어)
- **규칙**: 단수형 사용
- **예시**:
  ```sql
  member
  product
  cart
  wishlist
  search_log
  order_item
  ```

### 컬럼명
- **형식**: `snake_case`
- **규칙**:
  - ID 컬럼: `{테이블명}_id` (예: `member_id`, `product_id`)
  - 날짜/시간: `{동사}_at` (예: `created_at`, `updated_at`, `added_at`)
  - 불린: `is_{상태}` (예: `is_active`, `is_deleted`)

### 예시 테이블 구조
```sql
CREATE TABLE wishlist (
    member_id VARCHAR(100) NOT NULL,
    product_id VARCHAR(30) NOT NULL,
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id, product_id)
);

CREATE TABLE product (
    product_id VARCHAR(30) PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    product_price DECIMAL(10, 0) NOT NULL,
    product_category VARCHAR(30),
    product_company VARCHAR(30),
    product_stock INT DEFAULT 0,
    image_url VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## 2️⃣ Backend (Java/Spring Boot) 네이밍 규칙

### Entity 클래스
- **클래스명**: PascalCase (예: `Member`, `Product`, `Wishlist`)
- **필드명**: camelCase
- **어노테이션**: `@Column(name = "snake_case")`로 DB 매핑

```java
@Entity
@Table(name = "wishlist")
public class Wishlist {

    @Id
    @Column(name = "member_id", length = 100)
    private String memberId;

    @Id
    @Column(name = "product_id", length = 30)
    private String productId;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id",
                insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id",
                insertable = false, updatable = false)
    private Product product;
}
```

### DTO 클래스
- **클래스명**: `{도메인}Dto.{역할}` (예: `ProductDto.Response`, `WishlistDto.AddRequest`)
- **필드명**: camelCase
- **JSON 매핑**: `@JsonProperty("camelCase")` (프론트엔드와 동일)

```java
public class WishlistDto {

    @Getter
    @Setter
    public static class AddRequest {
        @JsonProperty("productId")
        private String productId;
    }

    @Getter
    @Builder
    public static class ItemResponse {
        @JsonProperty("productId")
        private String productId;

        @JsonProperty("productName")
        private String productName;

        @JsonProperty("productPrice")
        private BigDecimal productPrice;

        @JsonProperty("addedAt")
        private LocalDateTime addedAt;
    }
}
```

### Repository 메서드명
- **형식**: `{동사}By{필드명}And{필드명}`
- **필드명**: camelCase (Entity 필드명과 동일)
- **복잡한 쿼리**: `@Query` 어노테이션 사용

```java
public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {

    // 메서드 네이밍으로 쿼리 생성 (간단한 경우)
    List<Wishlist> findByMemberId(String memberId);

    // @Query 사용 (복잡한 경우 또는 명시적 쿼리)
    @Query("SELECT w FROM Wishlist w WHERE w.memberId = :memberId AND w.productId = :productId")
    boolean existsByMemberIdAndProductId(
        @Param("memberId") String memberId,
        @Param("productId") String productId
    );
}
```

### Service & Controller
- **클래스명**: `{도메인}Service`, `{도메인}Controller`
- **메서드명**: camelCase (예: `addToWishlist`, `getWishlist`)
- **변수명**: camelCase

```java
@Service
public class WishlistService {

    public void addToWishlist(String memberId, WishlistDto.AddRequest request) {
        String productId = request.getProductId();
        // ...
    }

    public WishlistDto.ListResponse getWishlist(String memberId) {
        List<Wishlist> wishlists = wishlistRepository.findByMemberId(memberId);
        return WishlistDto.ListResponse.from(wishlists);
    }
}
```

---

## 3️⃣ Frontend (TypeScript/React) 네이밍 규칙

### 타입/인터페이스
- **형식**: PascalCase
- **필드명**: camelCase (백엔드 JSON과 동일)

```typescript
export interface Product {
  productId: string;
  productName: string;
  productPrice: number;
  productCategory: string;
  productCompany: string;
  productStock: number;
  imageUrl?: string;
  createdAt?: string;
}

export interface WishlistItem {
  productId: string;
  productName: string;
  productPrice: number;
  productCategory: string;
  imageUrl: string;
  productStock: number;
  addedAt: string;
}
```

### 컴포넌트
- **파일명**: PascalCase (예: `ProductList.tsx`, `WishlistItem.tsx`)
- **컴포넌트명**: PascalCase
- **변수명**: camelCase
- **함수명**: camelCase

```typescript
const ProductList: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [wishlistStatus, setWishlistStatus] = useState<{[key: string]: boolean}>({});

  const handleProductClick = (productId: string) => {
    navigate(`/products/${productId}`);
  };

  const handleWishlistToggle = async (productId: string) => {
    // ...
  };

  return (
    <div className="product-list">
      {products.map((product) => (
        <ProductCard
          key={product.productId}
          product={product}
          isWishlisted={wishlistStatus[product.productId]}
          onWishlistToggle={() => handleWishlistToggle(product.productId)}
        />
      ))}
    </div>
  );
};
```

### API 호출
- **변수명**: camelCase
- **요청/응답**: JSON camelCase

```typescript
// 찜 추가 API 호출
const addToWishlist = async (productId: string) => {
  const token = localStorage.getItem('token');

  await axios.post(
    'http://localhost:8080/api/v1/wishlist',
    { productId },  // JSON: { "productId": "..." }
    {
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
    }
  );
};
```

---

## 4️⃣ JSON API 네이밍 규칙

### REST API 엔드포인트
- **형식**: kebab-case (소문자 + 하이픈)
- **예시**:
  ```
  GET    /api/v1/products
  GET    /api/v1/products/{productId}
  POST   /api/v1/wishlist
  DELETE /api/v1/wishlist/{productId}
  GET    /api/v1/search-logs
  ```

### JSON 필드명
- **형식**: camelCase
- **예시**:
  ```json
  {
    "productId": "P001",
    "productName": "오버핏 코트",
    "productPrice": 189000,
    "productCategory": "아우터",
    "productStock": 50,
    "imageUrl": "https://...",
    "addedAt": "2026-02-02T12:00:00"
  }
  ```

---

## 5️⃣ 전체 흐름 예시

### 찜하기 기능 (Wishlist)

```
1. Database (MySQL)
   테이블: wishlist
   컬럼: member_id, product_id, added_at

2. Backend Entity
   class Wishlist {
       memberId: String
       productId: String
       addedAt: LocalDateTime
   }

3. Backend DTO
   class WishlistDto.AddRequest {
       @JsonProperty("productId")
       productId: String
   }

4. JSON API
   POST /api/v1/wishlist
   Body: { "productId": "P001" }

5. Frontend Type
   interface WishlistAddRequest {
       productId: string;
   }

6. Frontend Code
   const addToWishlist = async (productId: string) => {
       await axios.post('/api/v1/wishlist', { productId });
   };
```

---

## 6️⃣ 마이그레이션 가이드

### 현재 상태 (Legacy)
```
DB: memberid, pID (혼재)
Backend: memberid, pID (혼재)
Frontend: memberId, pID (혼재)
```

### 목표 상태 (New)
```
DB: member_id, product_id (snake_case)
Backend: memberId, productId (camelCase)
Frontend: memberId, productId (camelCase)
JSON: memberId, productId (camelCase)
```

### 마이그레이션 단계

#### Phase 1: DB 컬럼 길이 수정 (긴급)
```sql
ALTER TABLE wishlist MODIFY COLUMN memberid VARCHAR(100);
ALTER TABLE cart MODIFY COLUMN memberid VARCHAR(100);
ALTER TABLE buy MODIFY COLUMN memberid VARCHAR(100);
```

#### Phase 2: DB 컬럼명 변경 (선택적, 기존 데이터 많으면 생략 가능)
```sql
-- 주의: 프로덕션에서는 신중하게 진행
ALTER TABLE wishlist CHANGE COLUMN memberid member_id VARCHAR(100);
ALTER TABLE wishlist CHANGE COLUMN pID product_id VARCHAR(30);
```

#### Phase 3: Backend Entity 수정
- `@Column(name = "member_id")` 추가
- 필드명을 camelCase로 변경
- DTO에 `@JsonProperty("camelCase")` 추가

#### Phase 4: Frontend 타입 수정
- 모든 타입을 camelCase로 통일

---

## 7️⃣ 체크리스트

### 새 Entity 추가 시
- [ ] DB 테이블명: snake_case
- [ ] DB 컬럼명: snake_case
- [ ] Entity 클래스명: PascalCase
- [ ] Entity 필드명: camelCase
- [ ] `@Column(name = "snake_case")` 어노테이션 추가

### 새 API 추가 시
- [ ] DTO 필드명: camelCase
- [ ] `@JsonProperty("camelCase")` 추가
- [ ] Frontend 타입: camelCase
- [ ] API 엔드포인트: kebab-case

### 기존 코드 수정 시
- [ ] 네이밍 규칙 준수 확인
- [ ] DB-Backend-Frontend 매핑 확인
- [ ] JSON 직렬화/역직렬화 테스트

---

## 8️⃣ 자주 하는 실수

### ❌ 잘못된 예
```java
// Entity 필드와 DB 컬럼명 불일치
@Column(name = "pID")  // DB는 snake_case여야 함
private String pID;

// JSON 필드명 불일치
@JsonProperty("pid")  // Frontend는 camelCase 기대
private String productId;
```

### ✅ 올바른 예
```java
// Entity
@Column(name = "product_id")
private String productId;

// DTO
@JsonProperty("productId")
private String productId;
```

---

## 9️⃣ 참고 자료

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript)
- [Spring Boot Naming Conventions](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [MySQL Naming Best Practices](https://dev.mysql.com/doc/refman/8.0/en/identifier-case-sensitivity.html)

---

## 📝 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|-----------|
| 2026-02-02 | 1.0 | 초안 작성 - 네이밍 규칙 정립 |
