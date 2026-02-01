# LookFit - TODO List

## 🔴 긴급 (High Priority)

### UI 버튼 색상 이슈
- **문제**: 상품 카드의 찜하기/AI 착장샷 버튼이 회색으로만 표시됨
- **위치**: ProductList 페이지 > 상품 카드
- **파일**: `frontend/src/styles/ProductList.css` - `.product-card__action-btn`
- **현재 상태**:
  - 버튼은 DOM에 존재하고 hover 효과는 작동
  - 기본 색상이 회색으로 보임
- **시도한 해결책**:
  - `color: var(--color-primary-600)` 추가
  - `border: 2px solid var(--color-neutral-300)` 추가
  - 브라우저 캐시 삭제 안내
- **다음 시도**:
  - SVG stroke 색상 직접 지정
  - !important 플래그 추가
  - 개발자 도구로 실제 적용된 CSS 확인

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
