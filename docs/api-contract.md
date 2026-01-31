# API Contract

> **작성자**: Claude (Backend)
> **최종 수정**: 2026-01-29
> **목적**: Gemini(Frontend/Design)가 API를 이해하고 UI를 설계할 수 있도록 스펙 제공

---

## 기본 정보

| 항목 | 값 |
|------|-----|
| Base URL | `http://localhost:8080` |
| API Prefix | `/api/v1` |
| 인증 방식 | JWT Bearer Token |
| Content-Type | `application/json` |

---

## 인증 (Authentication)

### 소셜 로그인 (Google OAuth2)

```
GET /oauth2/authorization/google
```

로그인 성공 시 리다이렉트:
```
GET /login/success?token={JWT_TOKEN}
```

### JWT 토큰 사용

모든 인증 필요 API 요청 시 헤더에 포함:
```
Authorization: Bearer {JWT_TOKEN}
```

---

## 회원 (Member)

### 회원 조회

```http
GET /api/members/{memberId}
```

**Response 200:**
```json
{
  "memberid": "user123",
  "membername": "홍길동",
  "gender": "M",
  "age": 25,
  "email": "user@example.com",
  "phone": "01012345678",
  "favorite": "캐주얼",
  "grade": "USER",
  "enrolldate": "2026-01-29T10:30:00",
  "regflag": "S"
}
```

**Response 404:** 회원 없음

### 회원 생성

```http
POST /api/members
Content-Type: application/json
```

**Request Body:**
```json
{
  "memberid": "user123",
  "membername": "홍길동",
  "gender": "M",
  "age": 25,
  "email": "user@example.com",
  "phone": "01012345678",
  "favorite": "캐주얼",
  "password": "hashedPassword123"
}
```

**Response 200:** 생성된 회원 정보 반환

---

## 상품 (Product) - 예정

### 상품 목록 조회

```http
GET /api/v1/products
GET /api/v1/products?category={category}&page={page}&size={size}
```

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| category | string | N | 카테고리 필터 |
| page | int | N | 페이지 번호 (default: 0) |
| size | int | N | 페이지 크기 (default: 20) |
| sort | string | N | 정렬 (price_asc, price_desc, newest) |

**Response 200:**
```json
{
  "content": [
    {
      "pID": "PROD001",
      "pname": "오버핏 후드티",
      "pprice": 45000,
      "pcategory": "상의",
      "description": "편안한 오버핏 후드티셔츠",
      "pcompany": "LookFit",
      "pstock": 100,
      "imageUrl": "/images/products/PROD001/main.webp",
      "thumbnailUrl": "/images/products/PROD001/thumb.webp"
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "currentPage": 0
}
```

### 상품 상세 조회

```http
GET /api/v1/products/{pID}
```

**Response 200:**
```json
{
  "pID": "PROD001",
  "pname": "오버핏 후드티",
  "pprice": 45000,
  "pcategory": "상의",
  "description": "편안한 오버핏 후드티셔츠",
  "pcompany": "LookFit",
  "pstock": 100,
  "images": [
    "/images/products/PROD001/main.webp",
    "/images/products/PROD001/gallery-1.webp",
    "/images/products/PROD001/gallery-2.webp"
  ],
  "sizes": ["S", "M", "L", "XL"],
  "colors": ["블랙", "화이트", "그레이"]
}
```

---

## 장바구니 (Cart) - 예정

### 장바구니 조회

```http
GET /api/v1/cart
Authorization: Bearer {token}
```

**Response 200:**
```json
{
  "items": [
    {
      "pID": "PROD001",
      "pname": "오버핏 후드티",
      "amount": 2,
      "pprice": 45000,
      "subtotal": 90000,
      "imageUrl": "/images/products/PROD001/thumb.webp"
    }
  ],
  "totalAmount": 2,
  "totalPrice": 90000
}
```

### 장바구니 추가

```http
POST /api/v1/cart
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "pID": "PROD001",
  "amount": 1,
  "size": "M",
  "color": "블랙"
}
```

**Response 201:** 추가된 장바구니 아이템

### 장바구니 수량 변경

```http
PATCH /api/v1/cart/{pID}
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "amount": 3
}
```

### 장바구니 삭제

```http
DELETE /api/v1/cart/{pID}
Authorization: Bearer {token}
```

**Response 204:** No Content

---

## 주문 (Order) - 예정

### 주문 생성

```http
POST /api/v1/orders
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "items": [
    { "pID": "PROD001", "amount": 2 }
  ],
  "shipping": {
    "resName": "홍길동",
    "resAddress": "서울시 강남구 테헤란로 123",
    "resPhone": "01012345678",
    "resRequirement": "문 앞에 놔주세요"
  },
  "paymentMethod": "CARD"
}
```

**Response 201:**
```json
{
  "orderno": 12345,
  "totalprice": 90000,
  "orderdate": "2026-01-29T15:30:00",
  "status": "PENDING"
}
```

### 주문 내역 조회

```http
GET /api/v1/orders
Authorization: Bearer {token}
```

### 주문 상세 조회

```http
GET /api/v1/orders/{orderno}
Authorization: Bearer {token}
```

---

## AI 착장샷 (Fitting) - 예정

### 사용자 사진 업로드

```http
POST /api/v1/fitting/photos
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Request:**
- `file`: 사용자 전신 사진 (jpg, png, webp)

**Response 201:**
```json
{
  "photoId": "photo_abc123",
  "photoUrl": "/images/users/{userId}/photos/photo_abc123.webp",
  "uploadedAt": "2026-01-29T15:30:00"
}
```

### AI 착장샷 생성 요청

```http
POST /api/v1/fitting/generate
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "photoId": "photo_abc123",
  "productIds": ["PROD001", "PROD002"]
}
```

**Response 202 (Accepted):**
```json
{
  "fittingId": "fit_xyz789",
  "status": "PROCESSING",
  "estimatedTime": 30
}
```

### 착장샷 결과 조회

```http
GET /api/v1/fitting/{fittingId}
Authorization: Bearer {token}
```

**Response 200 (완료 시):**
```json
{
  "fittingId": "fit_xyz789",
  "status": "COMPLETED",
  "resultUrl": "/images/fitting/{userId}/fit_xyz789.webp",
  "products": [
    { "pID": "PROD001", "pname": "오버핏 후드티" }
  ],
  "createdAt": "2026-01-29T15:30:00"
}
```

**Response 200 (처리 중):**
```json
{
  "fittingId": "fit_xyz789",
  "status": "PROCESSING",
  "progress": 60
}
```

---

## AI 코디 추천 - 예정

```http
POST /api/v1/recommend/outfit
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "occasion": "캐주얼",
  "season": "봄",
  "preferredStyle": ["미니멀", "스트릿"]
}
```

**Response 200:**
```json
{
  "recommendations": [
    {
      "outfitId": "outfit_001",
      "name": "봄 캐주얼 코디",
      "products": [
        { "pID": "PROD001", "pname": "오버핏 후드티", "role": "상의" },
        { "pID": "PROD005", "pname": "와이드 팬츠", "role": "하의" }
      ],
      "totalPrice": 89000,
      "previewUrl": "/images/outfits/outfit_001.webp"
    }
  ]
}
```

---

## 에러 응답 형식

모든 에러는 다음 형식으로 반환:

```json
{
  "timestamp": "2026-01-29T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_INPUT",
  "message": "잘못된 입력입니다",
  "path": "/api/v1/products"
}
```

### 에러 코드 목록

| HTTP Status | Code | 설명 |
|-------------|------|------|
| 400 | INVALID_INPUT | 잘못된 입력 |
| 401 | UNAUTHORIZED | 인증 필요 |
| 403 | FORBIDDEN | 권한 없음 |
| 404 | NOT_FOUND | 리소스 없음 |
| 409 | CONFLICT | 중복/충돌 |
| 422 | UNPROCESSABLE | 처리 불가 |
| 500 | INTERNAL_ERROR | 서버 에러 |

---

## 카테고리 코드

| 코드 | 한글명 |
|------|--------|
| TOP | 상의 |
| BOTTOM | 하의 |
| OUTER | 아우터 |
| DRESS | 원피스 |
| SHOES | 신발 |
| ACC | 액세서리 |

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|----------|
| 2026-01-29 | 0.1 | 초안 작성 |
