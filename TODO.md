# LookFit - TODO List

## 🔴 긴급 (High Priority)

### ~~UI 버튼 색상 이슈~~ ✅ 해결 (2026-02-02)
- **문제**: SVG 아이콘이 `0x20` (공백 문자)으로 렌더링되는 버그
- **원인**: React/Vite 환경에서 인라인 SVG가 공백 문자로 치환되는 렌더링 이슈
- **해결**: 유니코드 문자(♥, 👁)로 대체하여 정상 표시
- **파일**:
  - `frontend/src/components/ProductList.tsx`
  - `frontend/src/styles/ProductList.css`

## 🟡 중요 (Medium Priority)

### AI 착장샷 기능 구현 (Phase 4 - 프로젝트 핵심)
- [ ] AI 서비스 선정
  - Stable Diffusion 검토
  - Replicate API 검토
  - 비용 및 성능 비교
- [ ] 백엔드 API 설계
  - 사용자 사진 업로드 엔드포인트
  - AI 착장샷 생성 비동기 처리
  - 결과 이미지 저장 및 조회
- [ ] 프론트엔드 구현
  - VirtualFitting 페이지 완성
  - 사진 업로드 UI
  - 착장샷 결과 표시

### 찜하기 (Wishlist) 기능
- [ ] 백엔드 API 구현
  - Wishlist 엔티티 생성
  - CRUD API 엔드포인트
- [ ] 프론트엔드 연동
  - 찜하기 버튼 클릭 시 API 호출
  - 찜 목록 페이지

## 🟢 일반 (Low Priority)

### 코드 품질 개선
- [ ] 테스트 커버리지 향상
- [ ] API 문서 자동화 (Swagger/OpenAPI)
- [ ] 로깅 체계 개선

### 성능 최적화
- [ ] Redis 캐싱 도입
- [ ] 이미지 최적화 (WebP, lazy loading)
- [ ] DB 쿼리 최적화

### 추가 기능
- [ ] 리뷰 & Q&A 기능
- [ ] 검색 기능 강화 (Elasticsearch)
- [ ] 주문 취소/환불 기능

## ✅ 완료

### 2026-02-02
- [x] 장바구니 삭제 기능 수정 (Hibernate 네이밍 전략)
- [x] CartDto.ItemResponse에 @JsonProperty("pID") 추가
- [x] UI 버튼 아이콘 문제 해결 (유니코드 문자 사용)

### 2026-02-01
- [x] OAuth2 로그인 500 에러 수정 (enrolldate, 한글 인코딩)
- [x] 상품 이미지 표시 (imageUrl 필드 추가)
- [x] 장바구니 삭제 기능 구현
- [x] SecurityConfig "/login" 리다이렉트 제거
- [x] README.md 업데이트

### 2026-01-31
- [x] OAuth2 로그인 플로우 완성
- [x] Header 환영 메시지 구현
- [x] Puppeteer E2E 테스트

### 2026-01-30
- [x] 프론트엔드 전체 구현
- [x] 장바구니 기능 완전 구현
- [x] E2E 테스트 작성 및 실행

---

**마지막 업데이트**: 2026-02-01
