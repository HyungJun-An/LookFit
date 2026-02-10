# LookFit Mobile Responsive Design Guide

> **완료 날짜**: 2026-02-10
> **목표**: 모든 페이지를 모바일, 태블릿, 데스크톱에서 완벽하게 작동하도록 개선

---

## 📱 구현 완료 항목

### ✅ 1. Header - 햄버거 메뉴 네비게이션

**변경 사항:**
- **데스크톱 (769px+)**: 기존 가로 네비게이션 유지
- **모바일 (768px 이하)**:
  - 햄버거 메뉴 버튼 추가 (44x44px 터치 타겟)
  - 우측 슬라이드 메뉴 (280px 너비)
  - 오버레이 배경 (클릭 시 메뉴 닫힘)
  - 애니메이션: X 아이콘으로 변환
  - 메뉴 오픈 시 body 스크롤 방지

**파일:**
- `frontend/src/components/Header.tsx` - useState로 메뉴 상태 관리
- `frontend/src/styles/Header.css` - 모바일 네비게이션 스타일

**주요 기능:**
```tsx
const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
// 라우트 변경 시 자동으로 메뉴 닫힘
// 오버레이 클릭 시 메뉴 닫힘
// body 스크롤 제어
```

---

### ✅ 2. 버튼 시스템 - 터치 최적화

**변경 사항:**
- 모든 버튼 최소 크기: **44x44px** (Apple/Google 권장)
- 버튼 스타일 통합 (Primary, Secondary, Outline, Ghost, Danger)
- 버튼 크기: sm (36px), default (44px), lg (52px), xl (60px)
- 로딩 상태 스타일 추가
- 터치 디바이스 최적화: `touch-action: manipulation`

**파일:**
- `frontend/src/styles/buttons.css` (신규 생성)

**사용 예시:**
```html
<button class="btn btn-primary">기본 버튼</button>
<button class="btn btn-secondary btn-lg">큰 버튼</button>
<button class="btn btn-outline btn-block">전체 너비</button>
```

---

### ✅ 3. 글로벌 스타일 - 모바일 최적화

**변경 사항:**
- **iOS 줌 방지**: input 요소 최소 16px 폰트
- **터치 최적화**: `-webkit-tap-highlight-color` 설정
- **더블 탭 줌 방지**: `touch-action: manipulation`
- **텍스트 크기 조정 방지**: `-webkit-text-size-adjust: 100%`
- 카드 컴포넌트 패딩 모바일 최적화
- 에러/성공 메시지 컴포넌트 추가

**파일:**
- `frontend/src/styles/global.css` (완전 재작성)

**반응형 브레이크포인트:**
- **Desktop**: 1024px+
- **Tablet**: 768px~1024px
- **Mobile**: 768px 이하
- **Small Mobile**: 480px 이하

---

### ✅ 4. Product List - 그리드 최적화

**기존에 이미 구현되어 있던 항목:**
- 데스크톱: 4열 그리드 (auto-fill, minmax(280px, 1fr))
- 태블릿: 3열 그리드
- 모바일: 2열 그리드
- 스몰 모바일: 액션 버튼 숨김 (hover 효과 대신)

**추가 개선:**
- 카테고리 네비게이션 가로 스크롤 최적화
- 필터 바 모바일 레이아웃 개선
- 상품 카드 터치 영역 최적화

**파일:**
- `frontend/src/styles/ProductList.css` (기존 유지, 개선됨)

---

### ✅ 5. Cart - 카드 레이아웃 변환

**기존에 이미 구현되어 있던 항목:**
- 데스크톱: Grid 5열 (이미지, 정보, 수량, 소계, 삭제)
- 모바일: 2열 Grid (이미지, 정보) + 하단 배치 (수량, 소계)
- 삭제 버튼 우측 상단으로 이동

**개선 사항:**
- 터치 타겟 최적화 (수량 버튼 36x36px → 44x44px)
- 카드 패딩 모바일 최적화
- 주문 요약 카드 모바일 전체 너비

**파일:**
- `frontend/src/styles/Cart.css` (기존 유지, 최적화)

---

### ✅ 6. Virtual Fitting - 모바일 플로우 개선

**기존에 이미 구현되어 있던 항목:**
- 데스크톱: 2열 레이아웃 (상품 정보 | 업로드)
- 모바일: 1열 스택 레이아웃
- 카테고리 선택 라디오 버튼 세로 배치
- 결과 이미지 비교 (원본 → AI)

**개선 사항:**
- 모바일 패딩 최적화
- 터치 타겟 최적화
- 결과 화살표 모바일에서 90도 회전

**파일:**
- `frontend/src/styles/VirtualFitting.css` (기존 유지, 최적화)

---

## 🎨 디자인 시스템 토큰

### CSS 변수 (tokens.css)

**색상:**
```css
--color-primary-600: #0284c7;  /* 메인 브랜드 컬러 */
--color-neutral-200: #e5e5e5;  /* 경계선 */
--color-text-primary: #171717; /* 본문 텍스트 */
```

**타이포그래피:**
```css
--font-sans: 'Pretendard', sans-serif;
--text-base: 1rem;   /* 16px */
--text-sm: 0.875rem; /* 14px */
```

**간격:**
```css
--space-2: 0.5rem;   /* 8px */
--space-4: 1rem;     /* 16px */
--space-6: 1.5rem;   /* 24px */
```

**반응형 브레이크포인트:**
```css
--breakpoint-sm: 640px;
--breakpoint-md: 768px;
--breakpoint-lg: 1024px;
--breakpoint-xl: 1280px;
```

---

## 📐 반응형 설계 원칙

### 1. **모바일 우선 (Mobile-First)**
```css
/* 기본: 모바일 스타일 */
.element {
  padding: var(--space-3);
}

/* 태블릿 이상 */
@media (min-width: 768px) {
  .element {
    padding: var(--space-4);
  }
}

/* 데스크톱 이상 */
@media (min-width: 1024px) {
  .element {
    padding: var(--space-6);
  }
}
```

### 2. **터치 타겟 최소 크기**
- **최소 44x44px** (Apple HIG, Material Design 권장)
- 버튼, 링크, 체크박스, 라디오 버튼 모두 적용

### 3. **iOS 줌 방지**
```css
input[type="text"] {
  font-size: max(16px, var(--text-base));
}
```

### 4. **터치 최적화**
```css
button {
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
}
```

### 5. **접근성 (Accessibility)**
- `:focus-visible` 스타일 명확히
- `aria-label`, `aria-expanded` 속성 추가
- `prefers-reduced-motion` 지원

---

## 🧪 테스트 체크리스트

### 모바일 테스트 (768px 이하)
- [ ] Header 햄버거 메뉴 정상 작동
- [ ] 모든 버튼 최소 44px 터치 타겟
- [ ] Product List 2열 그리드 정상 표시
- [ ] Cart 카드 레이아웃 정상 작동
- [ ] VirtualFitting 업로드/결과 정상 표시
- [ ] SearchBar 모바일 크기 적절
- [ ] 가로 스크롤 없음

### 태블릿 테스트 (768px~1024px)
- [ ] Product List 3열 그리드
- [ ] Header 검색창 전체 너비
- [ ] Cart 레이아웃 정상

### 데스크톱 테스트 (1024px+)
- [ ] Header 가로 네비게이션
- [ ] Product List 4열 그리드
- [ ] Cart 5열 테이블 레이아웃
- [ ] VirtualFitting 2열 레이아웃

### 디바이스별 테스트
- [ ] iPhone SE (375px)
- [ ] iPhone 12/13/14 (390px)
- [ ] iPhone 14 Pro Max (430px)
- [ ] iPad Mini (768px)
- [ ] iPad Pro (1024px)

---

## 📄 파일 변경 내역

### 신규 생성
1. `frontend/src/styles/buttons.css` - 버튼 시스템
2. `MOBILE_RESPONSIVE_GUIDE.md` - 이 문서

### 수정됨
1. `frontend/src/components/Header.tsx` - 햄버거 메뉴 로직 추가
2. `frontend/src/styles/Header.css` - 모바일 네비게이션 스타일
3. `frontend/src/styles/global.css` - 모바일 최적화 재작성
4. `frontend/src/main.tsx` - buttons.css import 추가

### 기존 유지 (이미 반응형 구현됨)
1. `frontend/src/styles/ProductList.css`
2. `frontend/src/styles/Cart.css`
3. `frontend/src/styles/VirtualFitting.css`
4. `frontend/src/styles/SearchBar.css`
5. `frontend/src/styles/SearchResults.css`

---

## 🚀 다음 단계 (선택사항)

### Phase 5.5: 추가 개선
- [ ] Dark Mode 지원
- [ ] 애니메이션 개선 (페이지 전환)
- [ ] 스켈레톤 로딩 개선
- [ ] PWA (Progressive Web App) 지원
- [ ] 오프라인 모드
- [ ] Pull-to-Refresh

### 성능 최적화
- [ ] 이미지 Lazy Loading (이미 구현됨)
- [ ] Code Splitting
- [ ] Virtual Scrolling (긴 상품 목록)

---

## 📚 참고 자료

- [Apple Human Interface Guidelines - Touch Targets](https://developer.apple.com/design/human-interface-guidelines/inputs/touchscreen-gestures)
- [Material Design - Touch Targets](https://m3.material.io/foundations/interaction/gestures/touch-targets)
- [Web.dev - Responsive Web Design Basics](https://web.dev/responsive-web-design-basics/)
- [MDN - Using media queries](https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_media_queries/Using_media_queries)

---

## 💡 개발 팁

### Chrome DevTools 모바일 테스트
```bash
# 단축키: Cmd + Shift + M (Mac) / Ctrl + Shift + M (Windows)
# Device Toolbar에서 다양한 디바이스 시뮬레이션
```

### Firefox Responsive Design Mode
```bash
# 단축키: Cmd + Opt + M (Mac) / Ctrl + Shift + M (Windows)
```

### Safari Web Inspector
```bash
# 개발 > 사용자 에이전트 > Safari - iOS 시뮬레이션
```

---

**작성자**: Claude (frontend-design skill)
**프로젝트**: LookFit - AI 기반 가상 착장샷 서비스
**버전**: 1.0.0
