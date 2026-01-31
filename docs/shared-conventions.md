# Shared Conventions

> **목적**: Claude와 Gemini가 공유하는 규칙 및 디자인 토큰
> **최종 수정**: 2026-01-29

---

## 브랜드 아이덴티티

### 서비스명
- 정식: **LookFit**
- 태그라인: "AI가 입혀주는 나만의 스타일"

### 브랜드 톤
- 현대적, 깔끔한
- 신뢰감 있는
- 친근하지만 전문적인

---

## 디자인 토큰

### 색상 (Colors)

#### Primary (브랜드 메인)
| 토큰명 | HEX | 용도 |
|--------|-----|------|
| `--color-primary-50` | #f0f9ff | 배경 하이라이트 |
| `--color-primary-100` | #e0f2fe | 호버 배경 |
| `--color-primary-200` | #bae6fd | 보더 |
| `--color-primary-300` | #7dd3fc | 아이콘 |
| `--color-primary-400` | #38bdf8 | - |
| `--color-primary-500` | #0ea5e9 | **메인 컬러** |
| `--color-primary-600` | #0284c7 | 호버 |
| `--color-primary-700` | #0369a1 | 액티브 |
| `--color-primary-800` | #075985 | - |
| `--color-primary-900` | #0c4a6e | 텍스트 강조 |

#### Neutral (회색)
| 토큰명 | HEX | 용도 |
|--------|-----|------|
| `--color-neutral-50` | #fafafa | 페이지 배경 |
| `--color-neutral-100` | #f5f5f5 | 카드 배경 |
| `--color-neutral-200` | #e5e5e5 | 보더, 구분선 |
| `--color-neutral-300` | #d4d4d4 | 비활성 보더 |
| `--color-neutral-400` | #a3a3a3 | 플레이스홀더 |
| `--color-neutral-500` | #737373 | 보조 텍스트 |
| `--color-neutral-600` | #525252 | 본문 텍스트 |
| `--color-neutral-700` | #404040 | 제목 |
| `--color-neutral-800` | #262626 | 강조 텍스트 |
| `--color-neutral-900` | #171717 | 최고 강조 |

#### Semantic (의미적)
| 토큰명 | HEX | 용도 |
|--------|-----|------|
| `--color-success` | #22c55e | 성공, 완료 |
| `--color-warning` | #f59e0b | 경고 |
| `--color-error` | #ef4444 | 에러, 삭제 |
| `--color-info` | #3b82f6 | 정보 |

#### 배경 & 텍스트
| 토큰명 | HEX | 용도 |
|--------|-----|------|
| `--color-bg-primary` | #ffffff | 메인 배경 |
| `--color-bg-secondary` | #f5f5f5 | 섹션 배경 |
| `--color-bg-tertiary` | #e5e5e5 | 구분 배경 |
| `--color-text-primary` | #171717 | 메인 텍스트 |
| `--color-text-secondary` | #525252 | 보조 텍스트 |
| `--color-text-tertiary` | #a3a3a3 | 비활성 텍스트 |
| `--color-text-inverse` | #ffffff | 다크 배경 위 텍스트 |

---

### 타이포그래피 (Typography)

#### 폰트 패밀리
```css
--font-sans: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
--font-mono: 'JetBrains Mono', 'Fira Code', monospace;
```

#### 폰트 크기
| 토큰명 | 크기 | 용도 |
|--------|------|------|
| `--text-xs` | 12px / 0.75rem | 캡션, 뱃지 |
| `--text-sm` | 14px / 0.875rem | 보조 텍스트 |
| `--text-base` | 16px / 1rem | 본문 |
| `--text-lg` | 18px / 1.125rem | 강조 본문 |
| `--text-xl` | 20px / 1.25rem | 소제목 |
| `--text-2xl` | 24px / 1.5rem | 섹션 제목 |
| `--text-3xl` | 30px / 1.875rem | 페이지 제목 |
| `--text-4xl` | 36px / 2.25rem | 대제목 |
| `--text-5xl` | 48px / 3rem | 히어로 |

#### 폰트 굵기
| 토큰명 | 값 | 용도 |
|--------|-----|------|
| `--font-normal` | 400 | 본문 |
| `--font-medium` | 500 | 강조 본문 |
| `--font-semibold` | 600 | 소제목 |
| `--font-bold` | 700 | 제목 |

#### 행간
| 토큰명 | 값 | 용도 |
|--------|-----|------|
| `--leading-tight` | 1.25 | 제목 |
| `--leading-normal` | 1.5 | 본문 |
| `--leading-relaxed` | 1.75 | 긴 본문 |

---

### 간격 (Spacing)

| 토큰명 | 크기 | 용도 예시 |
|--------|------|----------|
| `--space-1` | 4px | 아이콘과 텍스트 사이 |
| `--space-2` | 8px | 인라인 요소 간격 |
| `--space-3` | 12px | 리스트 아이템 간격 |
| `--space-4` | 16px | 카드 패딩 |
| `--space-5` | 20px | - |
| `--space-6` | 24px | 섹션 패딩 |
| `--space-8` | 32px | 섹션 간 간격 |
| `--space-10` | 40px | - |
| `--space-12` | 48px | 대섹션 간격 |
| `--space-16` | 64px | 페이지 패딩 |
| `--space-20` | 80px | - |
| `--space-24` | 96px | 히어로 패딩 |

---

### 모서리 반경 (Border Radius)

| 토큰명 | 크기 | 용도 |
|--------|------|------|
| `--radius-none` | 0 | 직각 |
| `--radius-sm` | 2px | 작은 요소 |
| `--radius-md` | 6px | 버튼, 입력창 |
| `--radius-lg` | 8px | 카드 |
| `--radius-xl` | 12px | 모달 |
| `--radius-2xl` | 16px | 큰 카드 |
| `--radius-full` | 9999px | 원형, 필 |

---

### 그림자 (Shadow)

| 토큰명 | 값 | 용도 |
|--------|-----|------|
| `--shadow-sm` | 0 1px 2px rgba(0,0,0,0.05) | 미세한 입체감 |
| `--shadow-md` | 0 4px 6px rgba(0,0,0,0.1) | 카드 기본 |
| `--shadow-lg` | 0 10px 15px rgba(0,0,0,0.1) | 카드 호버 |
| `--shadow-xl` | 0 20px 25px rgba(0,0,0,0.1) | 모달 |

---

### 반응형 브레이크포인트

| 이름 | 크기 | 대상 디바이스 |
|------|------|--------------|
| sm | 640px | 큰 모바일 |
| md | 768px | 태블릿 |
| lg | 1024px | 작은 데스크탑 |
| xl | 1280px | 데스크탑 |
| 2xl | 1536px | 큰 데스크탑 |

**Mobile First 접근**:
```css
/* 기본: 모바일 */
.element { ... }

/* 태블릿 이상 */
@media (min-width: 768px) { ... }

/* 데스크탑 이상 */
@media (min-width: 1280px) { ... }
```

---

## 네이밍 규칙

### 파일명

| 유형 | 규칙 | 예시 |
|------|------|------|
| 컴포넌트 | PascalCase | `ProductCard.tsx`, `Button.vue` |
| 스타일 | kebab-case | `product-card.css` |
| 유틸리티 | camelCase | `formatPrice.ts` |
| 상수 | SCREAMING_SNAKE | `API_ENDPOINTS.ts` |
| 이미지 | kebab-case | `hero-banner.webp` |

### CSS 클래스

**BEM 방식 권장**:
```css
.block {}
.block__element {}
.block--modifier {}

/* 예시 */
.product-card {}
.product-card__image {}
.product-card__title {}
.product-card--featured {}
```

### API 엔드포인트

| 규칙 | 예시 |
|------|------|
| 복수형 명사 | `/products`, `/orders` |
| kebab-case | `/user-addresses` |
| 버전 포함 | `/api/v1/products` |
| 동사 지양 | ❌ `/getProducts` → ✅ `/products` |

---

## 이미지 규격

### 상품 이미지

| 용도 | 크기 | 비율 | 포맷 |
|------|------|------|------|
| 메인 | 1200x1600px | 3:4 | WebP |
| 썸네일 | 400x533px | 3:4 | WebP |
| 갤러리 | 800x1067px | 3:4 | WebP |

### AI 착장샷

| 용도 | 크기 | 포맷 |
|------|------|------|
| 입력 (사용자 사진) | 최소 512x768px | JPG, PNG, WebP |
| 출력 (결과) | 768x1024px | WebP |

### 아이콘

| 용도 | 크기 | 포맷 |
|------|------|------|
| UI 아이콘 | 24x24px | SVG |
| 작은 아이콘 | 16x16px | SVG |
| 큰 아이콘 | 32x32px | SVG |

---

## 접근성 (A11y)

### 필수 준수 사항

1. **색상 대비**: 최소 4.5:1 (WCAG AA)
2. **포커스 표시**: 모든 인터랙티브 요소에 `:focus-visible` 스타일
3. **대체 텍스트**: 모든 이미지에 `alt` 속성
4. **키보드 네비게이션**: Tab으로 모든 기능 접근 가능
5. **스크린 리더**: 적절한 ARIA 레이블

### 포커스 스타일
```css
:focus-visible {
  outline: 2px solid var(--color-primary-500);
  outline-offset: 2px;
}
```

---

## 애니메이션 규칙

### 지속시간
| 토큰명 | 값 | 용도 |
|--------|-----|------|
| `--duration-fast` | 150ms | 호버, 토글 |
| `--duration-normal` | 300ms | 모달, 드롭다운 |
| `--duration-slow` | 500ms | 페이지 전환 |

### 이징
| 토큰명 | 값 | 용도 |
|--------|-----|------|
| `--ease-in-out` | cubic-bezier(0.4, 0, 0.2, 1) | 기본 |
| `--ease-out` | cubic-bezier(0, 0, 0.2, 1) | 등장 |
| `--ease-in` | cubic-bezier(0.4, 0, 1, 1) | 퇴장 |

### 예시
```css
.card {
  transition: transform var(--duration-fast) var(--ease-out),
              box-shadow var(--duration-fast) var(--ease-out);
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}
```

---

## z-index 계층

| 레이어 | z-index | 요소 |
|--------|---------|------|
| Base | 0 | 일반 콘텐츠 |
| Dropdown | 100 | 드롭다운 메뉴 |
| Sticky | 200 | 고정 헤더 |
| Overlay | 300 | 오버레이 배경 |
| Modal | 400 | 모달 |
| Toast | 500 | 토스트 알림 |
| Tooltip | 600 | 툴팁 |

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 | 작성자 |
|------|------|----------|--------|
| 2026-01-29 | 1.0 | 초안 작성 | Claude |
