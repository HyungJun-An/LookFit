# LookFit Design System Rules

> **목적**: Figma 디자인을 코드로 변환할 때 일관성을 유지하기 위한 규칙

---

## 현재 상태

| 항목 | 상태 |
|------|------|
| Frontend Framework | 미정 (React/Vue/Thymeleaf 중 선택 필요) |
| CSS Framework | 미정 |
| Component Library | 미정 |
| Design Tokens | 미구현 |
| Figma Integration | 대기 중 |

---

## 권장 프로젝트 구조

### Option A: Spring Boot + Thymeleaf (Server-Side Rendering)

```
src/main/resources/
├── templates/                    # Thymeleaf HTML 템플릿
│   ├── layout/
│   │   └── default.html         # 기본 레이아웃
│   ├── fragments/               # 재사용 컴포넌트
│   │   ├── header.html
│   │   ├── footer.html
│   │   └── nav.html
│   ├── pages/
│   │   ├── home.html
│   │   ├── product/
│   │   ├── cart/
│   │   └── fitting/             # AI 착장샷
│   └── error/
│       └── 404.html
└── static/
    ├── css/
    │   ├── tokens/              # Design tokens
    │   │   ├── colors.css
    │   │   ├── typography.css
    │   │   └── spacing.css
    │   ├── components/          # 컴포넌트 스타일
    │   ├── pages/               # 페이지별 스타일
    │   └── main.css             # 진입점
    ├── js/
    │   ├── components/
    │   └── main.js
    ├── images/
    │   ├── icons/
    │   ├── logos/
    │   └── placeholders/
    └── fonts/
```

### Option B: Separate SPA (React/Vue + Vite)

```
frontend/                         # 별도 프론트엔드 디렉토리
├── src/
│   ├── assets/
│   │   ├── icons/
│   │   └── images/
│   ├── components/
│   │   ├── common/              # Button, Input, Card 등
│   │   ├── layout/              # Header, Footer, Sidebar
│   │   └── features/            # 기능별 컴포넌트
│   ├── pages/
│   ├── styles/
│   │   ├── tokens/
│   │   └── global.css
│   ├── hooks/
│   ├── services/                # API 호출
│   └── utils/
├── package.json
└── vite.config.js
```

---

## Design Tokens 정의

### 색상 (Colors)

```css
/* tokens/colors.css */
:root {
  /* Primary - 브랜드 메인 컬러 */
  --color-primary-50: #f0f9ff;
  --color-primary-100: #e0f2fe;
  --color-primary-200: #bae6fd;
  --color-primary-300: #7dd3fc;
  --color-primary-400: #38bdf8;
  --color-primary-500: #0ea5e9;  /* 메인 */
  --color-primary-600: #0284c7;
  --color-primary-700: #0369a1;
  --color-primary-800: #075985;
  --color-primary-900: #0c4a6e;

  /* Neutral - 회색 계열 */
  --color-neutral-50: #fafafa;
  --color-neutral-100: #f5f5f5;
  --color-neutral-200: #e5e5e5;
  --color-neutral-300: #d4d4d4;
  --color-neutral-400: #a3a3a3;
  --color-neutral-500: #737373;
  --color-neutral-600: #525252;
  --color-neutral-700: #404040;
  --color-neutral-800: #262626;
  --color-neutral-900: #171717;

  /* Semantic Colors */
  --color-success: #22c55e;
  --color-warning: #f59e0b;
  --color-error: #ef4444;
  --color-info: #3b82f6;

  /* Background */
  --color-bg-primary: #ffffff;
  --color-bg-secondary: #f5f5f5;
  --color-bg-tertiary: #e5e5e5;

  /* Text */
  --color-text-primary: #171717;
  --color-text-secondary: #525252;
  --color-text-tertiary: #a3a3a3;
  --color-text-inverse: #ffffff;
}
```

### 타이포그래피 (Typography)

```css
/* tokens/typography.css */
:root {
  /* Font Family */
  --font-sans: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  --font-mono: 'JetBrains Mono', 'Fira Code', monospace;

  /* Font Size */
  --text-xs: 0.75rem;     /* 12px */
  --text-sm: 0.875rem;    /* 14px */
  --text-base: 1rem;      /* 16px */
  --text-lg: 1.125rem;    /* 18px */
  --text-xl: 1.25rem;     /* 20px */
  --text-2xl: 1.5rem;     /* 24px */
  --text-3xl: 1.875rem;   /* 30px */
  --text-4xl: 2.25rem;    /* 36px */
  --text-5xl: 3rem;       /* 48px */

  /* Font Weight */
  --font-normal: 400;
  --font-medium: 500;
  --font-semibold: 600;
  --font-bold: 700;

  /* Line Height */
  --leading-tight: 1.25;
  --leading-normal: 1.5;
  --leading-relaxed: 1.75;

  /* Letter Spacing */
  --tracking-tight: -0.025em;
  --tracking-normal: 0;
  --tracking-wide: 0.025em;
}
```

### 간격 (Spacing)

```css
/* tokens/spacing.css */
:root {
  --space-0: 0;
  --space-1: 0.25rem;   /* 4px */
  --space-2: 0.5rem;    /* 8px */
  --space-3: 0.75rem;   /* 12px */
  --space-4: 1rem;      /* 16px */
  --space-5: 1.25rem;   /* 20px */
  --space-6: 1.5rem;    /* 24px */
  --space-8: 2rem;      /* 32px */
  --space-10: 2.5rem;   /* 40px */
  --space-12: 3rem;     /* 48px */
  --space-16: 4rem;     /* 64px */
  --space-20: 5rem;     /* 80px */
  --space-24: 6rem;     /* 96px */

  /* Border Radius */
  --radius-none: 0;
  --radius-sm: 0.125rem;  /* 2px */
  --radius-md: 0.375rem;  /* 6px */
  --radius-lg: 0.5rem;    /* 8px */
  --radius-xl: 0.75rem;   /* 12px */
  --radius-2xl: 1rem;     /* 16px */
  --radius-full: 9999px;

  /* Shadow */
  --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1);
  --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1);
  --shadow-xl: 0 20px 25px -5px rgb(0 0 0 / 0.1);
}
```

### 반응형 Breakpoints

```css
/* Breakpoints */
:root {
  --breakpoint-sm: 640px;
  --breakpoint-md: 768px;
  --breakpoint-lg: 1024px;
  --breakpoint-xl: 1280px;
  --breakpoint-2xl: 1536px;
}

/* Media Query Usage */
@media (min-width: 640px) { /* sm */ }
@media (min-width: 768px) { /* md */ }
@media (min-width: 1024px) { /* lg */ }
@media (min-width: 1280px) { /* xl */ }
```

---

## 컴포넌트 패턴

### Button 컴포넌트

```css
/* components/button.css */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  border-radius: var(--radius-md);
  transition: all 150ms ease;
  cursor: pointer;
}

.btn-primary {
  background-color: var(--color-primary-500);
  color: var(--color-text-inverse);
}

.btn-primary:hover {
  background-color: var(--color-primary-600);
}

.btn-secondary {
  background-color: var(--color-bg-secondary);
  color: var(--color-text-primary);
  border: 1px solid var(--color-neutral-300);
}

.btn-lg {
  padding: var(--space-3) var(--space-6);
  font-size: var(--text-base);
}

.btn-sm {
  padding: var(--space-1) var(--space-3);
  font-size: var(--text-xs);
}
```

### Card 컴포넌트

```css
/* components/card.css */
.card {
  background-color: var(--color-bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.card-header {
  padding: var(--space-4);
  border-bottom: 1px solid var(--color-neutral-200);
}

.card-body {
  padding: var(--space-4);
}

.card-footer {
  padding: var(--space-4);
  border-top: 1px solid var(--color-neutral-200);
  background-color: var(--color-bg-secondary);
}
```

### Product Card (LookFit 전용)

```css
/* components/product-card.css */
.product-card {
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: transform 200ms ease, box-shadow 200ms ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.product-card__image {
  aspect-ratio: 3/4;
  object-fit: cover;
  width: 100%;
}

.product-card__info {
  padding: var(--space-4);
}

.product-card__brand {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
}

.product-card__name {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--color-text-primary);
  margin-top: var(--space-1);
}

.product-card__price {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--color-primary-600);
  margin-top: var(--space-2);
}

/* AI 착장샷 버튼 */
.product-card__fitting-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  width: 100%;
  padding: var(--space-3);
  background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-600));
  color: var(--color-text-inverse);
  font-weight: var(--font-semibold);
  border: none;
  cursor: pointer;
  transition: opacity 200ms ease;
}

.product-card__fitting-btn:hover {
  opacity: 0.9;
}
```

---

## 아이콘 시스템

### 권장 아이콘 라이브러리
- **Heroicons** (Tailwind 친화적)
- **Lucide Icons** (React 친화적)
- **Feather Icons** (가벼움)

### 아이콘 사용 규칙

```html
<!-- SVG 직접 사용 (권장) -->
<svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor">
  <path d="..." />
</svg>

<!-- 아이콘 크기 클래스 -->
<style>
.icon { width: 1.5rem; height: 1.5rem; }
.icon-sm { width: 1rem; height: 1rem; }
.icon-lg { width: 2rem; height: 2rem; }
.icon-xl { width: 3rem; height: 3rem; }
</style>
```

### 아이콘 저장 위치
```
static/images/icons/
├── ui/                    # UI 아이콘 (arrow, close, menu 등)
├── social/                # 소셜 아이콘 (google, kakao 등)
├── ecommerce/             # 쇼핑 아이콘 (cart, heart, star 등)
└── fitting/               # AI 착장샷 관련 아이콘
```

---

## Figma → 코드 변환 규칙

### 1. 색상 매핑
| Figma Color Style | CSS Variable |
|-------------------|--------------|
| Primary/500 | `var(--color-primary-500)` |
| Neutral/900 | `var(--color-neutral-900)` |
| Background/Primary | `var(--color-bg-primary)` |

### 2. 타이포그래피 매핑
| Figma Text Style | CSS |
|------------------|-----|
| Heading/H1 | `font-size: var(--text-4xl); font-weight: var(--font-bold);` |
| Heading/H2 | `font-size: var(--text-3xl); font-weight: var(--font-bold);` |
| Body/Large | `font-size: var(--text-lg); font-weight: var(--font-normal);` |
| Body/Base | `font-size: var(--text-base); font-weight: var(--font-normal);` |
| Caption | `font-size: var(--text-sm); color: var(--color-text-secondary);` |

### 3. 간격 매핑
| Figma Spacing | CSS Variable |
|---------------|--------------|
| 4px | `var(--space-1)` |
| 8px | `var(--space-2)` |
| 16px | `var(--space-4)` |
| 24px | `var(--space-6)` |
| 32px | `var(--space-8)` |

### 4. Auto Layout → Flexbox
```css
/* Figma Auto Layout: Horizontal, Gap 16, Padding 24 */
.container {
  display: flex;
  flex-direction: row;
  gap: var(--space-4);      /* 16px */
  padding: var(--space-6);  /* 24px */
}

/* Figma Auto Layout: Vertical, Space Between */
.container {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
```

---

## 에셋 관리

### 이미지 최적화
- **제품 이미지**: WebP 포맷, 최대 1200x1600px
- **썸네일**: WebP 포맷, 400x533px
- **아이콘**: SVG (인라인 또는 스프라이트)
- **배경**: WebP 또는 AVIF

### 이미지 경로 규칙
```
/static/images/
├── products/           # 상품 이미지
│   └── {productId}/
│       ├── main.webp
│       ├── thumb.webp
│       └── gallery-{n}.webp
├── fitting/            # AI 착장샷 결과
│   └── {userId}/
│       └── {fittingId}.webp
├── users/              # 사용자 업로드 사진
│   └── {userId}/
│       └── photos/
└── banners/            # 배너 이미지
```

---

## 접근성 (Accessibility)

### 필수 규칙
1. 모든 이미지에 `alt` 속성 필수
2. 폼 요소에 `label` 연결 필수
3. 색상 대비 최소 4.5:1 (WCAG AA)
4. 키보드 네비게이션 지원
5. `focus` 상태 명확히 표시

```css
/* Focus 스타일 */
:focus-visible {
  outline: 2px solid var(--color-primary-500);
  outline-offset: 2px;
}

/* Skip to content */
.skip-link {
  position: absolute;
  top: -40px;
  left: 0;
  background: var(--color-primary-500);
  color: var(--color-text-inverse);
  padding: var(--space-2) var(--space-4);
  z-index: 100;
}

.skip-link:focus {
  top: 0;
}
```

---

## 다음 단계

1. [ ] Frontend framework 선택 (React/Vue/Thymeleaf)
2. [ ] CSS framework 선택 (Tailwind/Vanilla CSS)
3. [ ] Figma 디자인 파일 연결
4. [ ] 컴포넌트 라이브러리 구축
5. [ ] Storybook 설정 (컴포넌트 문서화)
6. [ ] 디자인 토큰 자동 동기화 설정

---

## 참고 자료

- [Figma Dev Mode](https://www.figma.com/dev-mode/)
- [Figma MCP Plugin](https://github.com/anthropics/mcp-figma)
- [Design Tokens W3C](https://design-tokens.github.io/community-group/)
