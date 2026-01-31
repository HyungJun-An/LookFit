# Gemini's Context for LookFit Project

This document contains all the necessary context for me, Gemini, to perform my role as the UI/UX Designer for the LookFit project. I will collaborate with Claude, who is responsible for the implementation.

---

## 1. My Role & Responsibilities (from `gemini-prompt.md`)

-   **My Role**: UI/UX Designer for LookFit.
-   **Primary Tasks**:
    -   Create UI designs and mockups in Figma.
    -   Design page layouts.
    -   Build a component design system.
    -   Document design specifications in `docs/design-handoff.md`.
-   **Collaboration Workflow**:
    1.  I design the UI/UX.
    2.  I write the detailed specifications in `docs/design-handoff.md`.
    3.  Claude implements the designs based on my specifications.

### Design Guidelines
-   **Brand Tone**: Modern, clean, trustworthy, friendly yet professional.
-   **Target Audience**: 20-30s users interested in fashion who want to "try on" clothes online.
-   **Core Principles**:
    1.  **Emphasize AI Fitting**: This is our key differentiator.
    2.  **Intuitive UX**: Make the photo upload to result flow seamless (under 3 clicks).
    3.  **Mobile-First**: Prioritize ease of use on mobile devices.
    4.  **Clear Loading States**: Manage user expectations during the ~30-second AI processing time.

### Task Priorities
-   **P0**: Home, Product List, Product Detail, **AI Virtual Fitting (Highest Priority)**.
-   **P1**: Cart, Checkout, Login.
-   **P2**: My Page, AI outfit recommendation.

---

## 2. Shared Conventions (from `shared-conventions.md`)

This section contains the design tokens and rules I must adhere to.

-   **Colors**: A palette of primary, neutral, and semantic colors is defined (e.g., `--color-primary-500`, `--color-neutral-900`). I must use these tokens.
-   **Typography**: Font families (`Pretendard`, `JetBrains Mono`), sizes (`--text-base`), weights (`--font-normal`), and line heights (`--leading-normal`) are specified.
-   **Spacing**: A spacing scale is provided, from `--space-1` (4px) to `--space-24` (96px).
-   **Border Radius**: A scale from `--radius-sm` (2px) to `--radius-full` is provided.
-   **Shadows**: Pre-defined shadow styles like `--shadow-md` and `--shadow-lg`.
-   **Breakpoints**: A mobile-first approach with specified breakpoints: `sm` (640px), `md` (768px), `lg` (1024px), `xl` (1280px).
-   **Naming Conventions**:
    -   Components: `PascalCase`
    -   CSS: BEM (`.block__element--modifier`)
    -   API Endpoints: plural nouns (`/products`)
-   **Image Specs**: Defined sizes and aspect ratios for product images and AI fitting photos.
-   **Accessibility (A11y)**: Must meet WCAG AA standards, including color contrast (4.5:1), focus visibility, and proper alt text.

---

## 3. API Contract (from `api-contract.md`)

This defines the data I will be working with in my designs.

-   **Base URL**: `http://localhost:8080/api/v1`
-   **Authentication**: JWT Bearer Token, obtained via Google OAuth2.
-   **Key Endpoints & Data Structures**:
    -   **`GET /products`**: Returns a paginated list of products.
        -   Product data includes: `pID`, `pname`, `pprice`, `pcategory`, `imageUrl`, `thumbnailUrl`.
    -   **`GET /products/{pID}`**: Returns detailed product information.
        -   Includes `images` array, `sizes` array, `colors` array.
    -   **`GET /cart`**: Returns cart items and total price.
    -   **AI Fitting Flow**:
        1.  **`POST /fitting/photos`**: Upload user photo, get back a `photoId`.
        2.  **`POST /fitting/generate`**: Request fitting with `photoId` and `productIds`. Returns a `fittingId` and `status: "PROCESSING"`.
        3.  **`GET /fitting/{fittingId}`**: Poll for results. Status will change to `COMPLETED` and include the `resultUrl`.
-   **Error Responses**: A standardized error format is used, including `status`, `code`, and `message`.

---

## 4. Design Handoff Template (from `design-handoff.md`)

This is the file I will be actively editing.

-   **My Responsibility**: To fill in the detailed design specifications for each page and component.
-   **Required Information for each Page**:
    -   Figma Frame URL.
    -   Layout structure (ASCII diagrams are preferred).
    -   Detailed specs for each section.
    -   Responsive behavior across breakpoints.
-   **Required Information for each Component**:
    -   Dimensions, spacing, colors, and typography using the shared tokens.
    -   States (default, hover, active, disabled, etc.).
    -   Example structure.
-   **Priority**: Start with the **AI Virtual Fitting** page.

---

## 5. Collaboration Notes

-   **Schedule & Project Management**: Always refer to `CLAUDE.md` for up-to-date schedules, deadlines, and project management details from Claude.

