# LookFit Naming Rules for Claude Agents

> **중요**: 모든 Claude 에이전트는 코드 작성 시 이 규칙을 **반드시** 따라야 합니다.

---

## 📌 핵심 규칙 요약

| Layer | Convention | Example |
|-------|------------|---------|
| **Database** | snake_case | `member_id`, `product_id`, `added_at` |
| **Java Entity** | camelCase | `memberId`, `productId`, `addedAt` |
| **JSON API** | camelCase | `"memberId"`, `"productId"`, `"addedAt"` |
| **TypeScript** | camelCase | `memberId`, `productId`, `addedAt` |

---

## 🚨 필수 체크사항

### 1. Entity 작성 시
```java
@Entity
@Table(name = "wishlist")  // snake_case
public class Wishlist {

    @Column(name = "member_id", length = 100)  // snake_case
    private String memberId;  // camelCase

    @Column(name = "product_id", length = 30)
    private String productId;

    @Column(name = "added_at")
    private LocalDateTime addedAt;
}
```

### 2. DTO 작성 시
```java
public class WishlistDto {

    @Getter
    @Setter
    public static class AddRequest {
        @JsonProperty("productId")  // camelCase (프론트와 동일)
        private String productId;
    }

    @Getter
    @Builder
    public static class Response {
        @JsonProperty("productId")
        private String productId;

        @JsonProperty("productName")
        private String productName;
    }
}
```

### 3. Repository 작성 시
```java
public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {

    // 간단한 쿼리: 메서드명 (camelCase)
    List<Wishlist> findByMemberId(String memberId);

    // 복잡한 쿼리: @Query 사용
    @Query("SELECT w FROM Wishlist w WHERE w.memberId = :memberId AND w.productId = :productId")
    boolean existsByMemberIdAndProductId(
        @Param("memberId") String memberId,
        @Param("productId") String productId
    );
}
```

### 4. TypeScript 타입 작성 시
```typescript
export interface Product {
  productId: string;      // camelCase (백엔드 JSON과 동일)
  productName: string;
  productPrice: number;
  productCategory: string;
  productStock: number;
  imageUrl?: string;
  addedAt?: string;
}
```

---

## ❌ 절대 하지 말 것

### 1. DB 컬럼명에 camelCase 사용
```java
❌ @Column(name = "productId")  // 잘못됨
✅ @Column(name = "product_id")  // 올바름
```

### 2. Entity 필드명에 snake_case 사용
```java
❌ private String product_id;  // 잘못됨
✅ private String productId;   // 올바름
```

### 3. JSON 필드와 TypeScript 타입 불일치
```typescript
// Backend DTO
@JsonProperty("productId")  // ✅
private String productId;

// Frontend Type
productId: string;  // ✅ (일치함)
```

---

## 🔧 마이그레이션 중인 Legacy 코드

### 현재 상태 (Legacy)
일부 기존 코드는 아직 구 네이밍을 사용합니다:
- `memberid` (DB & Java) → `member_id` / `memberId`로 변경 예정
- `pID` (DB & Java) → `product_id` / `productId`로 변경 예정

### 신규 코드 작성 시
- **무조건 새로운 규칙(camelCase)** 적용
- 기존 코드 참조 시 주의

---

## 📋 작업 체크리스트

새 기능 추가 시:
- [ ] DB 스키마: snake_case 사용
- [ ] Entity: `@Column(name = "snake_case")` + camelCase 필드
- [ ] DTO: `@JsonProperty("camelCase")` 추가
- [ ] TypeScript: camelCase 타입 정의
- [ ] 테스트: JSON 직렬화/역직렬화 확인

---

## 📄 전체 문서

상세한 규칙은 프로젝트 루트의 `NAMING_CONVENTION.md` 참조
