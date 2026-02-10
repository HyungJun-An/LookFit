# LookFit - TODO List

## 🔴 긴급 (High Priority)

### 없음 ✅
현재 긴급한 이슈가 없습니다.

---

## 🟡 중요 (Medium Priority)

### Phase 5: QA & 안정화
- [ ] **전체 시스템 통합 테스트**
  - 모든 기능 E2E 테스트 실행
  - 성능 부하 테스트
  - 보안 취약점 스캔 (OWASP Top 10)
- [ ] **성능 최적화**
  - DB 쿼리 최적화 (N+1 문제 해결)
  - 이미지 lazy loading
  - Redis 캐싱 도입 (검색 결과, 상품 목록)
- [ ] **보안 점검**
  - XSS/CSRF 방어 검증
  - SQL Injection 방어 확인
  - 파일 업로드 검증 강화
- [ ] **배포 준비**
  - Docker Compose 프로덕션 설정
  - 환경변수 암호화
  - 로깅 체계 개선

### Phase 6: 배포 & 운영
- [ ] **이미지 S3 마이그레이션**
  - 로컬 이미지 → S3 업로드
  - CloudFront CDN 설정
  - 이미지 URL 업데이트
- [ ] **AWS 배포**
  - EC2 인스턴스 설정
  - RDS MySQL 마이그레이션
  - Elasticsearch Service 설정
  - Load Balancer 구성
- [ ] **CI/CD 파이프라인**
  - GitHub Actions 워크플로우
  - 자동 빌드 및 테스트
  - Blue-Green 배포
- [ ] **모니터링**
  - CloudWatch 로그
  - 에러 알림 (Slack/Email)
  - APM 도구 연동 (New Relic / DataDog)

---

## 🟢 일반 (Low Priority)

### 코드 품질 개선
- [ ] API 문서 자동화 (Swagger/OpenAPI)
- [ ] 주석 및 JavaDoc 추가
- [ ] 코드 리뷰 체크리스트 작성
- [ ] SonarQube 정적 분석

### 추가 기능
- [ ] **주문 관리**
  - 주문 취소 기능
  - 환불 처리
  - 배송 추적
- [ ] **관리자 페이지**
  - 상품 관리 (CRUD)
  - 주문 관리
  - 회원 관리
  - 통계 대시보드
- [ ] **마이페이지 개선**
  - 회원정보 수정
  - 비밀번호 변경
  - 탈퇴 기능
- [ ] **소셜 로그인 확장**
  - 카카오 로그인
  - 네이버 로그인
- [ ] **푸시 알림**
  - 주문 상태 변경 알림
  - 찜한 상품 가격 변동 알림
  - 리뷰 작성 유도 알림

### 성능 최적화
- [ ] 이미지 WebP 변환
- [ ] DB 인덱스 최적화
- [ ] 페이지네이션 커서 기반으로 변경

---

## ✅ 완료

### 2026-02-11 (Phase 4.6)
- [x] **리뷰 & 별점 기능 완전 구현** ⭐
  - Review 엔티티 생성
  - ReviewRepository (평균 별점, 리뷰 수 쿼리)
  - ReviewService (구매 확인, 이미지 업로드, XSS 방지)
  - ReviewController (5개 REST API)
  - StarRating 컴포넌트 (읽기/쓰기 모드)
  - ReviewForm 컴포넌트 (별점 입력, 텍스트, 이미지 업로드)
  - ReviewList 컴포넌트 (목록, 페이징, 평균 별점)
  - ProductDetail에 리뷰 섹션 통합
  - 단위 테스트 11개 작성
  - E2E 테스트 7개 작성
  - README.md, CLAUDE.md 업데이트

### 2026-02-10 (Phase 4.5)
- [x] **모바일 반응형 UI 완전 구현** 📱
  - Header 햄버거 메뉴 (모바일 슬라이드 네비게이션)
  - 통합 버튼 시스템 (buttons.css)
  - 글로벌 스타일 모바일 최적화 (global.css)
  - Product List 반응형 그리드 (2/3/4열)
  - Cart 모바일 카드 레이아웃
  - VirtualFitting 모바일 1열 스택
  - iOS 줌 방지, 터치 최적화
  - E2E 테스트 작성
  - MOBILE_RESPONSIVE_GUIDE.md 작성

### 2026-02-10 (Phase 4)
- [x] **AI 가상 착장샷 기능 완전 구현** 🤖
  - Hugging Face IDM-VTON 모델 연동
  - Python Gradio Client 통합
  - VirtualFittingService 구현
  - HuggingFaceGradioService 구현
  - FittingController (4개 REST API)
  - VirtualFitting 페이지 (업로드 → AI 생성 → 결과 표시)
  - 이미지 형식 변환 (AVIF → JPEG)
  - GPU 할당량 초과 예외 처리

### 2026-02-02 (Phase 3.5)
- [x] **Elasticsearch 검색 기능 완전 구현** 🔍
  - Elasticsearch 8.17.0 + Nori 한글 분석기
  - ProductDocument 인덱스 자동 생성
  - SearchService (키워드 검색, 필터, 정렬)
  - ProductIndexService (인덱스 관리, 비동기 재인덱싱)
  - SearchController (Public API)
  - AdminSearchController (관리자 API)
  - SearchBar + SearchResults 페이지
  - 인기 검색어 + 최근 검색어 추천
  - E2E 테스트 10개 작성

### 2026-02-02
- [x] 장바구니 삭제 기능 수정 (Hibernate 네이밍 전략)
- [x] CartDto.ItemResponse에 @JsonProperty("pID") 추가
- [x] UI 버튼 아이콘 문제 해결 (유니코드 문자 사용)

### 2026-02-01 (Phase 3)
- [x] **주문 기능 완전 구현**
  - OrderController + OrderService
  - OrderItem 엔티티 추가
  - 재고 관리, 트랜잭션 처리
  - E2E 테스트 7개 작성
- [x] **MySQL UTF-8 인코딩 완전 수정**
- [x] **OAuth2 로그인 500 에러 수정**
- [x] 상품 이미지 표시 (imageUrl 필드)
- [x] 장바구니 삭제 기능 구현
- [x] SecurityConfig "/login" 리다이렉트 제거
- [x] E2E 테스트 51개 작성 및 100% 통과

### 2026-01-31 (Phase 2)
- [x] OAuth2 로그인 플로우 완성
- [x] Header 환영 메시지 구현
- [x] Puppeteer E2E 테스트

### 2026-01-30 (Phase 1)
- [x] 프론트엔드 전체 구현
- [x] 장바구니 기능 완전 구현
- [x] E2E 테스트 작성 및 실행
- [x] DDD 구조로 리팩토링
- [x] Google OAuth2 소셜 로그인
- [x] JWT 토큰 발급 및 검증

---

## 📊 프로젝트 현황

### 완료된 기능
- ✅ 인증 (OAuth2 + JWT)
- ✅ 상품 목록/상세
- ✅ 검색 (Elasticsearch + Nori)
- ✅ 장바구니
- ✅ 주문
- ✅ 찜 목록
- ✅ **AI 가상 피팅** 🤖
- ✅ **모바일 반응형** 📱
- ✅ **리뷰 & 별점** ⭐

### 테스트 커버리지
- **58개 E2E 테스트 100% 통과**
- Shopping Flow: 9/9
- Product Detail: 13/13
- Order API: 7/7
- Image Loading: 8/9
- Cart Flow: 4/4
- Search Flow: 10/10
- Review Flow: 7/7

### 다음 단계
- 🔜 Phase 5: QA & 안정화
- 🔜 Phase 6: 배포 & 운영

---

**마지막 업데이트**: 2026-02-11
