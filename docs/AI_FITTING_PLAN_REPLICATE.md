# AI 가상 피팅 구현 계획 (Replicate)

> **선택 사항**: Replicate API (무료 크레딧 → 유료) + 로컬 이미지 저장 → S3 마이그레이션

---

## 🎯 구현 목표

사용자가 자신의 전신 사진과 원하는 상품을 선택하면, AI가 해당 옷을 입은 모습을 생성해주는 가상 피팅 서비스

---

## 🛠 기술 스택

| 항목 | 선택 | 이유 |
|------|------|------|
| **AI API** | Replicate | 빠른 속도 (10-30초), 무료 크레딧, Webhook 지원 |
| **모델** | IDM-VTON | SOTA 성능, Virtual Try-On 전문 |
| **이미지 저장** | 로컬 → S3 | 빠른 개발 후 마이그레이션 |
| **처리 방식** | 비동기 (@Async) | 긴 처리 시간 대응 |
| **상태 확인** | 폴링 (3초마다) | 실시간 진행률 표시 |

---

## 📋 Phase 1: 인프라 준비 (즉시 시작)

### 1.1. Replicate API 설정

#### Step 1: 가입 및 크레딧 확인
```bash
1. https://replicate.com 방문
2. Sign up (GitHub 계정 연동 가능)
3. Account Settings → API Tokens
4. "Create token" 클릭
5. 무료 크레딧 확인 (처음 가입 시 제공)
```

#### Step 2: 환경변수 설정
```yaml
# backend/src/main/resources/application.yml
replicate:
  api:
    key: ${REPLICATE_API_KEY}
    base-url: https://api.replicate.com/v1
    webhook-url: ${REPLICATE_WEBHOOK_URL:http://localhost:8080/api/v1/fitting/webhook}

file:
  upload:
    dir: ${user.dir}/src/main/resources/static/images/fitting
    max-size: 10MB
    allowed-extensions: jpg,jpeg,png,webp
```

```bash
# .env (로컬 개발 - Git 제외)
REPLICATE_API_KEY=r8_xxxxxxxxxxxxxxxxxxxxx
```

### 1.2. 로컬 이미지 디렉토리 생성

```bash
# 디렉토리 구조
backend/src/main/resources/static/images/fitting/
├── user/           # 사용자 업로드 이미지
│   └── {memberId}/
│       └── {timestamp}_original.jpg
└── result/         # AI 생성 결과 이미지
    └── {memberId}/
        └── {fittingId}_result.png
```

#### 디렉토리 생성 스크립트
```bash
#!/bin/bash
# scripts/create-fitting-directories.sh

FITTING_DIR="backend/src/main/resources/static/images/fitting"

mkdir -p "$FITTING_DIR/user"
mkdir -p "$FITTING_DIR/result"

echo "✅ AI 피팅 디렉토리 생성 완료!"
echo "   - $FITTING_DIR/user/"
echo "   - $FITTING_DIR/result/"
```

---

## 📋 Phase 2: 데이터베이스 설계

### 2.1. Entity 생성

#### VirtualFitting.java
```java
package com.lookfit.fitting.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "virtual_fitting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VirtualFitting {

    @Id
    @Column(name = "fitting_id", length = 36)
    private String fittingId; // UUID

    @Column(name = "member_id", length = 100, nullable = false)
    private String memberId;

    @Column(name = "product_id", length = 30, nullable = false)
    private String productId;

    @Column(name = "user_image_url", length = 500, nullable = false)
    private String userImageUrl; // /images/fitting/user/{memberId}/xxx.jpg

    @Column(name = "result_image_url", length = 500)
    private String resultImageUrl; // /images/fitting/result/{memberId}/xxx.png

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private FittingStatus status = FittingStatus.PENDING;

    @Column(name = "category", length = 20)
    private String category; // upper_body, lower_body, dresses

    @Column(name = "replicate_prediction_id", length = 100)
    private String replicatePredictionId; // Replicate 작업 ID

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // 상태 변경 메서드
    public void updateStatus(FittingStatus status) {
        this.status = status;
        if (status == FittingStatus.COMPLETED || status == FittingStatus.FAILED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public void updateReplicatePredictionId(String predictionId) {
        this.replicatePredictionId = predictionId;
    }

    public void updateResultImageUrl(String resultImageUrl) {
        this.resultImageUrl = resultImageUrl;
    }

    public void updateErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
```

#### FittingStatus.java
```java
package com.lookfit.fitting.domain;

public enum FittingStatus {
    PENDING,      // 요청 생성됨
    PROCESSING,   // AI 생성 중
    COMPLETED,    // 완료
    FAILED        // 실패
}
```

### 2.2. Repository

```java
package com.lookfit.fitting.repository;

import com.lookfit.fitting.domain.FittingStatus;
import com.lookfit.fitting.domain.VirtualFitting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VirtualFittingRepository extends JpaRepository<VirtualFitting, String> {

    // 사용자별 피팅 기록 조회 (최신순)
    List<VirtualFitting> findByMemberIdOrderByCreatedAtDesc(String memberId);

    // 특정 상품의 피팅 기록 조회
    List<VirtualFitting> findByProductId(String productId);

    // 상태별 조회 (관리자용)
    List<VirtualFitting> findByStatus(FittingStatus status);

    // Replicate Prediction ID로 조회
    Optional<VirtualFitting> findByReplicatePredictionId(String predictionId);
}
```

### 2.3. DB 마이그레이션 SQL

```sql
-- scripts/sql/create_virtual_fitting_table.sql
CREATE TABLE virtual_fitting (
    fitting_id VARCHAR(36) PRIMARY KEY,
    member_id VARCHAR(100) NOT NULL,
    product_id VARCHAR(30) NOT NULL,
    user_image_url VARCHAR(500) NOT NULL,
    result_image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    category VARCHAR(20),
    replicate_prediction_id VARCHAR(100),
    error_message VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME,

    INDEX idx_member_id (member_id),
    INDEX idx_product_id (product_id),
    INDEX idx_status (status),
    INDEX idx_replicate_prediction_id (replicate_prediction_id),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 📋 Phase 3: 백엔드 구현 (우선순위: A → B → C)

### Priority A: 이미지 업로드 기능

#### FittingDto.java
```java
package com.lookfit.fitting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lookfit.fitting.domain.FittingStatus;
import lombok.*;
import java.util.List;

public class FittingDto {

    // 이미지 업로드 응답
    @Getter
    @Builder
    public static class UploadResponse {
        @JsonProperty("imageUrl")
        private String imageUrl;

        @JsonProperty("fileName")
        private String fileName;

        @JsonProperty("fileSize")
        private Long fileSize;

        @JsonProperty("uploadedAt")
        private String uploadedAt;
    }

    // AI 피팅 생성 요청
    @Getter
    @Setter
    public static class GenerateRequest {
        @JsonProperty("productId")
        private String productId;

        @JsonProperty("userImageUrl")
        private String userImageUrl;
    }

    // AI 피팅 생성 응답
    @Getter
    @Builder
    public static class GenerateResponse {
        @JsonProperty("fittingId")
        private String fittingId;

        @JsonProperty("status")
        private FittingStatus status;

        @JsonProperty("message")
        private String message;

        @JsonProperty("estimatedTime")
        private Integer estimatedTime; // 예상 소요 시간 (초)
    }

    // 피팅 결과 조회 응답
    @Getter
    @Builder
    public static class FittingResponse {
        @JsonProperty("fittingId")
        private String fittingId;

        @JsonProperty("memberId")
        private String memberId;

        @JsonProperty("productId")
        private String productId;

        @JsonProperty("productName")
        private String productName;

        @JsonProperty("productImageUrl")
        private String productImageUrl;

        @JsonProperty("userImageUrl")
        private String userImageUrl;

        @JsonProperty("resultImageUrl")
        private String resultImageUrl;

        @JsonProperty("status")
        private FittingStatus status;

        @JsonProperty("category")
        private String category;

        @JsonProperty("errorMessage")
        private String errorMessage;

        @JsonProperty("createdAt")
        private String createdAt;

        @JsonProperty("completedAt")
        private String completedAt;
    }

    // 피팅 히스토리 응답
    @Getter
    @Builder
    public static class HistoryResponse {
        @JsonProperty("fittings")
        private List<FittingResponse> fittings;

        @JsonProperty("totalCount")
        private Integer totalCount;
    }
}
```

#### VirtualFittingController.java (Priority A: 업로드만)
```java
package com.lookfit.fitting.controller;

import com.lookfit.fitting.dto.FittingDto;
import com.lookfit.fitting.service.VirtualFittingService;
import com.lookfit.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/fitting")
@RequiredArgsConstructor
@Slf4j
public class VirtualFittingController {

    private final VirtualFittingService fittingService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Priority A: 사용자 이미지 업로드
     * POST /api/v1/fitting/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<FittingDto.UploadResponse> uploadUserImage(
            @RequestHeader("Authorization") String token,
            @RequestParam("image") MultipartFile image
    ) throws IOException {
        String memberId = extractMemberIdFromToken(token);

        log.info("Image upload request - memberId: {}, size: {} bytes", memberId, image.getSize());

        FittingDto.UploadResponse response = fittingService.uploadUserImage(memberId, image);
        return ResponseEntity.ok(response);
    }

    // JWT에서 memberId 추출
    private String extractMemberIdFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        return jwtTokenProvider.getMemberIdFromToken(jwt);
    }
}
```

#### VirtualFittingService.java (Priority A: 업로드만)
```java
package com.lookfit.fitting.service;

import com.lookfit.fitting.dto.FittingDto;
import com.lookfit.global.exception.BusinessException;
import com.lookfit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VirtualFittingService {

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${file.upload.max-size:10485760}") // 10MB
    private Long maxFileSize;

    // Priority A: 사용자 이미지 업로드
    @Transactional
    public FittingDto.UploadResponse uploadUserImage(String memberId, MultipartFile file) throws IOException {
        // 1. 파일 검증
        validateImageFile(file);

        // 2. 저장 디렉토리 생성
        String userDir = uploadDir + "/user/" + memberId;
        Files.createDirectories(Paths.get(userDir));

        // 3. 파일명 생성 (타임스탬프 + 확장자)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = timestamp + "_original." + extension;
        String filePath = userDir + "/" + fileName;

        // 4. 파일 저장
        Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);

        // 5. URL 생성
        String imageUrl = "/images/fitting/user/" + memberId + "/" + fileName;

        log.info("User image uploaded successfully: {} (size: {} bytes)", imageUrl, file.getSize());

        return FittingDto.UploadResponse.builder()
                .imageUrl(imageUrl)
                .fileName(fileName)
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now().toString())
                .build();
    }

    // === 유틸리티 메서드 ===

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일이 비어있습니다.");
        }

        // 파일 크기 검증
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("파일 크기는 %dMB를 초과할 수 없습니다.", maxFileSize / 1024 / 1024));
        }

        // 확장자 검증
        String extension = getFileExtension(file.getOriginalFilename());
        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "webp");
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "지원하지 않는 파일 형식입니다. (jpg, jpeg, png, webp만 가능)");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 파일명입니다.");
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
```

---

## 📋 실행 계획

### Step 1: Replicate 가입 (지금 즉시)
1. https://replicate.com 방문
2. 회원가입 및 API 토큰 발급
3. 무료 크레딧 확인

### Step 2: 백엔드 준비 (10분)
```bash
# 1. 디렉토리 생성
./scripts/create-fitting-directories.sh

# 2. DB 테이블 생성
docker exec -i lookfit-mysql mysql -u root -p651212 lookfit_db < scripts/sql/create_virtual_fitting_table.sql

# 3. 환경변수 설정
echo "REPLICATE_API_KEY=r8_your_token_here" >> backend/.env
```

### Step 3: 코드 작성 (Priority A: 업로드)
- [x] VirtualFitting Entity
- [x] FittingStatus Enum
- [x] VirtualFittingRepository
- [x] FittingDto
- [x] VirtualFittingService (uploadUserImage)
- [x] VirtualFittingController (upload endpoint)

### Step 4: 테스트
```bash
# 백엔드 재시작
cd backend
./gradlew bootRun

# API 테스트
curl -X POST http://localhost:8080/api/v1/fitting/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "image=@test-photo.jpg"
```

---

## 🎯 다음 단계 (Priority B, C)

구현 순서: **A → B → C**

- **Priority A** (지금): 이미지 업로드 ✅
- **Priority B** (다음): AI 피팅 생성 (Replicate API 연동)
- **Priority C** (마지막): 피팅 히스토리

---

**지금 바로 시작하시겠습니까?** 🚀
