# AI 가상 피팅 구현 계획 (Hugging Face)

> **선택 사항**: Hugging Face Inference API (완전 무료) + 로컬 이미지 저장

---

## 📋 Phase 1: 인프라 준비 (1일)

### 1.1. Hugging Face API 설정

#### 가입 및 토큰 발급
```bash
1. https://huggingface.co 가입
2. Settings → Access Tokens → New token 생성
3. Token 권한: "Read" (무료)
```

#### 환경변수 설정
```bash
# backend/src/main/resources/application.yml
huggingface:
  api:
    token: ${HUGGINGFACE_API_TOKEN}
    base-url: https://api-inference.huggingface.co/models
```

```bash
# .env (로컬 개발)
HUGGINGFACE_API_TOKEN=hf_xxxxxxxxxxxxxxxxxxxxx
```

### 1.2. 로컬 이미지 저장 디렉토리 생성

```bash
# 디렉토리 구조
backend/src/main/resources/static/images/
├── fitting/
│   ├── user/           # 사용자 업로드 이미지
│   │   └── {memberId}/
│   │       └── {timestamp}_original.jpg
│   └── result/         # AI 생성 결과 이미지
│       └── {memberId}/
│           └── {fittingId}_result.jpg
```

#### application.yml 설정
```yaml
file:
  upload:
    dir: ${user.dir}/src/main/resources/static/images/fitting
    max-size: 10MB  # 최대 파일 크기
    allowed-extensions: jpg,jpeg,png,webp
```

---

## 📋 Phase 2: 데이터베이스 설계 (0.5일)

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
    private String userImageUrl; // 로컬: /images/fitting/user/{memberId}/xxx.jpg

    @Column(name = "result_image_url", length = 500)
    private String resultImageUrl; // 로컬: /images/fitting/result/{memberId}/xxx.jpg

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private FittingStatus status;

    @Column(name = "category", length = 20)
    private String category; // upper_body, lower_body, dresses

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

import com.lookfit.fitting.domain.VirtualFitting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VirtualFittingRepository extends JpaRepository<VirtualFitting, String> {

    // 사용자별 피팅 기록 조회 (최신순)
    List<VirtualFitting> findByMemberIdOrderByCreatedAtDesc(String memberId);

    // 특정 상품의 피팅 기록 조회
    List<VirtualFitting> findByProductId(String productId);

    // 상태별 조회 (관리자용)
    List<VirtualFitting> findByStatus(FittingStatus status);
}
```

### 2.3. DB 마이그레이션 SQL

```sql
-- create_virtual_fitting_table.sql
CREATE TABLE virtual_fitting (
    fitting_id VARCHAR(36) PRIMARY KEY,
    member_id VARCHAR(100) NOT NULL,
    product_id VARCHAR(30) NOT NULL,
    user_image_url VARCHAR(500) NOT NULL,
    result_image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    category VARCHAR(20),
    error_message VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME,

    INDEX idx_member_id (member_id),
    INDEX idx_product_id (product_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 📋 Phase 3: 백엔드 구현 (2-3일)

### 3.1. DTO 클래스

```java
package com.lookfit.fitting.dto;

import com.lookfit.fitting.domain.FittingStatus;
import lombok.*;

public class FittingDto {

    // 이미지 업로드 응답
    @Getter
    @Builder
    public static class UploadResponse {
        private String imageUrl;      // /images/fitting/user/{memberId}/xxx.jpg
        private String fileName;
        private Long fileSize;
        private String uploadedAt;
    }

    // AI 피팅 생성 요청
    @Getter
    @Setter
    public static class GenerateRequest {
        private String productId;
        private String userImageUrl;  // 업로드된 이미지 URL
        private String category;      // "상의", "하의", "원피스" → 자동 매핑
    }

    // AI 피팅 생성 응답
    @Getter
    @Builder
    public static class GenerateResponse {
        private String fittingId;
        private FittingStatus status;
        private String message;       // "AI 피팅 이미지를 생성 중입니다..."
        private Integer estimatedTime; // 예상 소요 시간 (초)
    }

    // 피팅 결과 조회 응답
    @Getter
    @Builder
    public static class FittingResponse {
        private String fittingId;
        private String memberId;
        private String productId;
        private String productName;
        private String productImageUrl;
        private String userImageUrl;
        private String resultImageUrl;
        private FittingStatus status;
        private String category;
        private String errorMessage;
        private String createdAt;
        private String completedAt;
    }

    // 피팅 히스토리 응답
    @Getter
    @Builder
    public static class HistoryResponse {
        private List<FittingResponse> fittings;
        private Integer totalCount;
    }
}
```

### 3.2. Service Layer

```java
package com.lookfit.fitting.service;

import com.lookfit.fitting.domain.FittingStatus;
import com.lookfit.fitting.domain.VirtualFitting;
import com.lookfit.fitting.dto.FittingDto;
import com.lookfit.fitting.repository.VirtualFittingRepository;
import com.lookfit.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class VirtualFittingService {

    private final VirtualFittingRepository fittingRepository;
    private final ProductRepository productRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${huggingface.api.token}")
    private String huggingfaceApiToken;

    @Value("${huggingface.api.base-url}")
    private String huggingfaceBaseUrl;

    @Value("${file.upload.dir}")
    private String uploadDir;

    // 1. 사용자 이미지 업로드
    @Transactional
    public FittingDto.UploadResponse uploadUserImage(String memberId, MultipartFile file) throws IOException {
        // 파일 검증
        validateImageFile(file);

        // 저장 디렉토리 생성
        String userDir = uploadDir + "/user/" + memberId;
        Files.createDirectories(Paths.get(userDir));

        // 파일명 생성 (타임스탬프 + 확장자)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = timestamp + "_original." + extension;
        String filePath = userDir + "/" + fileName;

        // 파일 저장
        Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);

        // URL 생성 (/images/fitting/user/{memberId}/xxx.jpg)
        String imageUrl = "/images/fitting/user/" + memberId + "/" + fileName;

        log.info("User image uploaded: {} (size: {} bytes)", imageUrl, file.getSize());

        return FittingDto.UploadResponse.builder()
                .imageUrl(imageUrl)
                .fileName(fileName)
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now().toString())
                .build();
    }

    // 2. AI 피팅 생성 요청
    @Transactional
    public FittingDto.GenerateResponse generateFitting(String memberId, FittingDto.GenerateRequest request) {
        // UUID 생성
        String fittingId = UUID.randomUUID().toString();

        // 상품 정보 조회
        var product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 카테고리 매핑 (한글 → 영어)
        String category = mapCategory(product.getPcategory());

        // VirtualFitting 엔티티 생성 (status: PENDING)
        VirtualFitting fitting = VirtualFitting.builder()
                .fittingId(fittingId)
                .memberId(memberId)
                .productId(request.getProductId())
                .userImageUrl(request.getUserImageUrl())
                .category(category)
                .status(FittingStatus.PENDING)
                .build();

        fittingRepository.save(fitting);

        // 비동기로 AI 생성 시작
        generateFittingAsync(fittingId, request.getUserImageUrl(), product.getImageurl(), category);

        return FittingDto.GenerateResponse.builder()
                .fittingId(fittingId)
                .status(FittingStatus.PENDING)
                .message("AI 피팅 이미지를 생성 중입니다. 1-2분 정도 소요될 수 있습니다.")
                .estimatedTime(90) // 예상 90초
                .build();
    }

    // 3. 비동기 AI 생성
    @Async
    @Transactional
    public CompletableFuture<Void> generateFittingAsync(
            String fittingId,
            String userImageUrl,
            String productImageUrl,
            String category
    ) {
        log.info("Starting AI fitting generation: fittingId={}", fittingId);

        try {
            // 상태 업데이트: PROCESSING
            VirtualFitting fitting = fittingRepository.findById(fittingId)
                    .orElseThrow(() -> new IllegalArgumentException("Fitting not found"));
            fitting.updateStatus(FittingStatus.PROCESSING);
            fittingRepository.save(fitting);

            // Hugging Face API 호출
            String resultImageUrl = callHuggingFaceAPI(
                    convertToAbsolutePath(userImageUrl),
                    convertToAbsolutePath(productImageUrl),
                    category
            );

            // 결과 이미지 저장
            String savedResultUrl = saveResultImage(fitting.getMemberId(), fittingId, resultImageUrl);

            // 상태 업데이트: COMPLETED
            fitting.updateStatus(FittingStatus.COMPLETED);
            fitting.updateResultImageUrl(savedResultUrl);
            fittingRepository.save(fitting);

            log.info("AI fitting completed: fittingId={}, resultUrl={}", fittingId, savedResultUrl);

        } catch (Exception e) {
            log.error("AI fitting generation failed: fittingId={}", fittingId, e);

            // 상태 업데이트: FAILED
            VirtualFitting fitting = fittingRepository.findById(fittingId).orElseThrow();
            fitting.updateStatus(FittingStatus.FAILED);
            fitting.updateErrorMessage(e.getMessage());
            fittingRepository.save(fitting);
        }

        return CompletableFuture.completedFuture(null);
    }

    // 4. Hugging Face API 호출
    private String callHuggingFaceAPI(String userImagePath, String productImagePath, String category) throws IOException {
        // Hugging Face Inference API 호출
        // 모델: yisol/IDM-VTON 또는 levihsu/OOTDiffusion

        String apiUrl = huggingfaceBaseUrl + "/yisol/IDM-VTON";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + huggingfaceApiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 body (Hugging Face API 형식)
        Map<String, Object> requestBody = Map.of(
                "inputs", Map.of(
                        "image", readImageAsBase64(userImagePath),
                        "garment_image", readImageAsBase64(productImagePath),
                        "category", category
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // API 호출 (응답이 이미지 바이트)
        ResponseEntity<byte[]> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                byte[].class
        );

        // 응답 이미지를 Base64로 변환하여 반환
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(response.getBody());
    }

    // 5. 결과 이미지 저장
    private String saveResultImage(String memberId, String fittingId, String base64Image) throws IOException {
        // Base64 디코딩
        String base64Data = base64Image.split(",")[1];
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        // 저장 디렉토리 생성
        String resultDir = uploadDir + "/result/" + memberId;
        Files.createDirectories(Paths.get(resultDir));

        // 파일명 생성
        String fileName = fittingId + "_result.png";
        String filePath = resultDir + "/" + fileName;

        // 파일 저장
        Files.write(Paths.get(filePath), imageBytes);

        // URL 생성
        String imageUrl = "/images/fitting/result/" + memberId + "/" + fileName;

        log.info("Result image saved: {}", imageUrl);
        return imageUrl;
    }

    // 6. 피팅 결과 조회
    @Transactional(readOnly = true)
    public FittingDto.FittingResponse getFitting(String fittingId) {
        VirtualFitting fitting = fittingRepository.findById(fittingId)
                .orElseThrow(() -> new IllegalArgumentException("피팅 기록을 찾을 수 없습니다."));

        var product = productRepository.findById(fitting.getProductId()).orElse(null);

        return FittingDto.FittingResponse.builder()
                .fittingId(fitting.getFittingId())
                .memberId(fitting.getMemberId())
                .productId(fitting.getProductId())
                .productName(product != null ? product.getPname() : null)
                .productImageUrl(product != null ? product.getImageurl() : null)
                .userImageUrl(fitting.getUserImageUrl())
                .resultImageUrl(fitting.getResultImageUrl())
                .status(fitting.getStatus())
                .category(fitting.getCategory())
                .errorMessage(fitting.getErrorMessage())
                .createdAt(fitting.getCreatedAt().toString())
                .completedAt(fitting.getCompletedAt() != null ? fitting.getCompletedAt().toString() : null)
                .build();
    }

    // 7. 내 피팅 기록 조회
    @Transactional(readOnly = true)
    public FittingDto.HistoryResponse getMyFittings(String memberId) {
        List<VirtualFitting> fittings = fittingRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

        List<FittingDto.FittingResponse> responses = fittings.stream()
                .map(fitting -> {
                    var product = productRepository.findById(fitting.getProductId()).orElse(null);
                    return FittingDto.FittingResponse.builder()
                            .fittingId(fitting.getFittingId())
                            .productId(fitting.getProductId())
                            .productName(product != null ? product.getPname() : null)
                            .productImageUrl(product != null ? product.getImageurl() : null)
                            .userImageUrl(fitting.getUserImageUrl())
                            .resultImageUrl(fitting.getResultImageUrl())
                            .status(fitting.getStatus())
                            .createdAt(fitting.getCreatedAt().toString())
                            .build();
                })
                .toList();

        return FittingDto.HistoryResponse.builder()
                .fittings(responses)
                .totalCount(responses.size())
                .build();
    }

    // === 유틸 메서드 ===

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "webp");
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, webp만 가능)");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String mapCategory(String koreanCategory) {
        return switch (koreanCategory) {
            case "상의" -> "upper_body";
            case "하의" -> "lower_body";
            case "원피스", "치마" -> "dresses";
            default -> "upper_body";
        };
    }

    private String convertToAbsolutePath(String relativeUrl) {
        // /images/fitting/user/xxx.jpg → /Users/.../static/images/fitting/user/xxx.jpg
        return System.getProperty("user.dir") + "/src/main/resources/static" + relativeUrl;
    }

    private String readImageAsBase64(String imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
```

### 3.3. Controller

```java
package com.lookfit.fitting.controller;

import com.lookfit.fitting.dto.FittingDto;
import com.lookfit.fitting.service.VirtualFittingService;
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

    /**
     * 1. 사용자 이미지 업로드
     * POST /api/v1/fitting/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<FittingDto.UploadResponse> uploadUserImage(
            @RequestHeader("Authorization") String token,
            @RequestParam("image") MultipartFile image
    ) throws IOException {
        // JWT에서 memberId 추출
        String memberId = extractMemberIdFromToken(token);

        FittingDto.UploadResponse response = fittingService.uploadUserImage(memberId, image);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. AI 피팅 생성 요청
     * POST /api/v1/fitting/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<FittingDto.GenerateResponse> generateFitting(
            @RequestHeader("Authorization") String token,
            @RequestBody FittingDto.GenerateRequest request
    ) {
        String memberId = extractMemberIdFromToken(token);

        FittingDto.GenerateResponse response = fittingService.generateFitting(memberId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 피팅 결과 조회 (폴링용)
     * GET /api/v1/fitting/{fittingId}
     */
    @GetMapping("/{fittingId}")
    public ResponseEntity<FittingDto.FittingResponse> getFitting(
            @PathVariable String fittingId
    ) {
        FittingDto.FittingResponse response = fittingService.getFitting(fittingId);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. 내 피팅 기록 조회
     * GET /api/v1/fitting/history
     */
    @GetMapping("/history")
    public ResponseEntity<FittingDto.HistoryResponse> getMyFittings(
            @RequestHeader("Authorization") String token
    ) {
        String memberId = extractMemberIdFromToken(token);

        FittingDto.HistoryResponse response = fittingService.getMyFittings(memberId);
        return ResponseEntity.ok(response);
    }

    // JWT에서 memberId 추출 (간단 구현)
    private String extractMemberIdFromToken(String token) {
        // TODO: JwtTokenProvider 사용
        return "test@test.com"; // 임시
    }
}
```

---

## 📋 Phase 4: 프론트엔드 구현 (2일)

### 4.1. VirtualFitting 컴포넌트

```typescript
// frontend/src/components/VirtualFitting.tsx
import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';
import { useAuth } from '../context/AuthContext';
import '../styles/VirtualFitting.css';

interface Product {
  productId: string;
  productName: string;
  productPrice: number;
  productCategory: string;
  imageUrl: string;
}

interface FittingResult {
  fittingId: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  resultImageUrl?: string;
  errorMessage?: string;
}

const VirtualFitting = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { memberId, isAuthenticated } = useAuth();

  // State
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(
    location.state?.product || null
  );
  const [userImage, setUserImage] = useState<File | null>(null);
  const [userImagePreview, setUserImagePreview] = useState<string>('');
  const [userImageUrl, setUserImageUrl] = useState<string>('');

  const [step, setStep] = useState<'upload' | 'select' | 'generate' | 'result'>('upload');
  const [status, setStatus] = useState<'idle' | 'uploading' | 'processing' | 'completed' | 'failed'>('idle');

  const [fittingId, setFittingId] = useState<string | null>(null);
  const [resultImageUrl, setResultImageUrl] = useState<string>('');
  const [progress, setProgress] = useState<number>(0);

  // 로그인 체크
  useEffect(() => {
    if (!isAuthenticated) {
      alert('로그인이 필요한 서비스입니다.');
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  // Step 1: 사용자 이미지 업로드
  const handleImageUpload = async () => {
    if (!userImage) return;

    try {
      setStatus('uploading');

      const formData = new FormData();
      formData.append('image', userImage);

      const response = await axiosInstance.post('/api/v1/fitting/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      setUserImageUrl(response.data.imageUrl);
      setStep('select');
      setStatus('idle');

    } catch (error) {
      console.error('Image upload failed:', error);
      alert('이미지 업로드에 실패했습니다.');
      setStatus('idle');
    }
  };

  // Step 2: AI 피팅 생성
  const handleGenerateFitting = async () => {
    if (!selectedProduct || !userImageUrl) return;

    try {
      setStatus('processing');
      setStep('generate');
      setProgress(0);

      // AI 피팅 요청
      const response = await axiosInstance.post('/api/v1/fitting/generate', {
        productId: selectedProduct.productId,
        userImageUrl: userImageUrl,
      });

      setFittingId(response.data.fittingId);

      // 폴링 시작 (5초마다)
      const interval = setInterval(async () => {
        try {
          const result = await axiosInstance.get<FittingResult>(
            `/api/v1/fitting/${response.data.fittingId}`
          );

          // 진행률 시뮬레이션 (실제론 백엔드에서 받아야 함)
          setProgress(prev => Math.min(prev + 5, 95));

          if (result.data.status === 'COMPLETED') {
            setResultImageUrl(result.data.resultImageUrl!);
            setStatus('completed');
            setStep('result');
            setProgress(100);
            clearInterval(interval);
          } else if (result.data.status === 'FAILED') {
            alert('AI 피팅 생성에 실패했습니다: ' + result.data.errorMessage);
            setStatus('failed');
            clearInterval(interval);
          }
        } catch (error) {
          console.error('Polling error:', error);
          clearInterval(interval);
        }
      }, 5000);

    } catch (error) {
      console.error('Fitting generation failed:', error);
      alert('AI 피팅 생성에 실패했습니다.');
      setStatus('failed');
    }
  };

  // UI 렌더링
  return (
    <div className="virtual-fitting-container">
      <h1 className="fitting-title">AI 가상 피팅</h1>

      {/* Progress Bar */}
      <div className="fitting-steps">
        <div className={`step ${step === 'upload' ? 'active' : 'completed'}`}>1. 내 사진 업로드</div>
        <div className={`step ${step === 'select' ? 'active' : step === 'generate' || step === 'result' ? 'completed' : ''}`}>2. 상품 선택</div>
        <div className={`step ${step === 'generate' ? 'active' : step === 'result' ? 'completed' : ''}`}>3. AI 생성</div>
        <div className={`step ${step === 'result' ? 'active' : ''}`}>4. 결과 확인</div>
      </div>

      {/* Step 1: 이미지 업로드 */}
      {step === 'upload' && (
        <div className="upload-section">
          <h2>전신 사진을 업로드해주세요</h2>
          <p className="upload-tip">
            • 전신이 잘 보이는 사진을 선택하세요<br />
            • 흰 배경이나 단색 배경이 좋습니다<br />
            • 최대 10MB, JPG/PNG 형식
          </p>

          <div className="image-upload-box">
            {userImagePreview ? (
              <img src={userImagePreview} alt="Preview" className="preview-image" />
            ) : (
              <div className="upload-placeholder">
                <span className="upload-icon">📷</span>
                <p>클릭하여 이미지 선택</p>
              </div>
            )}
            <input
              type="file"
              accept="image/jpeg,image/jpg,image/png,image/webp"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) {
                  setUserImage(file);
                  setUserImagePreview(URL.createObjectURL(file));
                }
              }}
            />
          </div>

          <button
            className="btn-primary"
            disabled={!userImage || status === 'uploading'}
            onClick={handleImageUpload}
          >
            {status === 'uploading' ? '업로드 중...' : '다음 단계'}
          </button>
        </div>
      )}

      {/* Step 2: 상품 선택 */}
      {step === 'select' && (
        <div className="product-selection">
          <h2>입어볼 상품을 선택하세요</h2>
          {selectedProduct ? (
            <div className="selected-product">
              <img src={selectedProduct.imageUrl} alt={selectedProduct.productName} />
              <h3>{selectedProduct.productName}</h3>
              <button className="btn-primary" onClick={handleGenerateFitting}>
                AI 피팅 생성하기
              </button>
            </div>
          ) : (
            <p>상품을 선택해주세요</p>
          )}
        </div>
      )}

      {/* Step 3: 생성 중 */}
      {step === 'generate' && status === 'processing' && (
        <div className="generating-section">
          <div className="loading-spinner"></div>
          <h2>AI가 가상 피팅 이미지를 생성하고 있습니다...</h2>
          <div className="progress-bar">
            <div className="progress-fill" style={{ width: `${progress}%` }}></div>
          </div>
          <p className="progress-text">{progress}% 완료 (약 1-2분 소요)</p>
          <p className="waiting-tip">
            💡 잠시만 기다려주세요. Hugging Face 무료 API를 사용하여<br />
            생성 시간이 다소 소요될 수 있습니다.
          </p>
        </div>
      )}

      {/* Step 4: 결과 */}
      {step === 'result' && status === 'completed' && (
        <div className="result-section">
          <h2>🎉 AI 피팅 완성!</h2>
          <div className="result-images">
            <div className="result-image-box">
              <h3>내 사진</h3>
              <img src={userImagePreview} alt="User" />
            </div>
            <div className="arrow">→</div>
            <div className="result-image-box">
              <h3>AI 피팅 결과</h3>
              <img src={resultImageUrl} alt="Result" />
            </div>
          </div>
          <div className="result-actions">
            <button className="btn-secondary" onClick={() => window.location.reload()}>
              다시 시도
            </button>
            <button className="btn-primary" onClick={() => navigate('/cart')}>
              장바구니에 담기
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default VirtualFitting;
```

### 4.2. CSS 스타일

```css
/* frontend/src/styles/VirtualFitting.css */
.virtual-fitting-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.fitting-title {
  text-align: center;
  font-size: 2.5rem;
  margin-bottom: 40px;
  color: #1a1a1a;
}

/* Progress Steps */
.fitting-steps {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 60px;
}

.step {
  padding: 12px 24px;
  border-radius: 8px;
  background: #f5f5f5;
  color: #999;
  font-weight: 500;
}

.step.active {
  background: #0ea5e9;
  color: white;
}

.step.completed {
  background: #22c55e;
  color: white;
}

/* Image Upload */
.upload-section {
  text-align: center;
}

.upload-tip {
  color: #666;
  line-height: 1.8;
  margin-bottom: 30px;
}

.image-upload-box {
  position: relative;
  width: 400px;
  height: 500px;
  margin: 0 auto 30px;
  border: 2px dashed #ddd;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.3s;
}

.image-upload-box:hover {
  border-color: #0ea5e9;
}

.image-upload-box input[type="file"] {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

.upload-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

/* Generating */
.generating-section {
  text-align: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 60px;
  height: 60px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #0ea5e9;
  border-radius: 50%;
  margin: 0 auto 30px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.progress-bar {
  width: 400px;
  height: 24px;
  background: #f5f5f5;
  border-radius: 12px;
  margin: 30px auto;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #0ea5e9, #38bdf8);
  transition: width 0.5s ease;
}

.progress-text {
  font-size: 1.2rem;
  font-weight: 600;
  color: #0ea5e9;
  margin-bottom: 20px;
}

.waiting-tip {
  color: #666;
  line-height: 1.8;
}

/* Result */
.result-section {
  text-align: center;
}

.result-images {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 40px;
  margin: 40px 0;
}

.result-image-box {
  text-align: center;
}

.result-image-box h3 {
  margin-bottom: 20px;
  font-size: 1.2rem;
}

.result-image-box img {
  width: 400px;
  height: 500px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.arrow {
  font-size: 3rem;
  color: #0ea5e9;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
}

/* Buttons */
.btn-primary, .btn-secondary {
  padding: 14px 32px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: #0ea5e9;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0284c7;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3);
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background: white;
  color: #0ea5e9;
  border: 2px solid #0ea5e9;
}

.btn-secondary:hover {
  background: #f0f9ff;
}
```

---

## 📋 Phase 5: 로컬 → S3 마이그레이션 준비 (나중에)

### 5.1. 마이그레이션 스크립트

```bash
#!/bin/bash
# scripts/migrate-fitting-images-to-s3.sh

echo "🚀 가상 피팅 이미지 S3 마이그레이션 시작..."

# 로컬 이미지 디렉토리
LOCAL_DIR="backend/src/main/resources/static/images/fitting"
S3_BUCKET="lookfit-fitting-images"
S3_REGION="ap-northeast-2"

# User images
echo "📦 사용자 이미지 업로드 중..."
aws s3 sync "$LOCAL_DIR/user/" "s3://$S3_BUCKET/fitting/user/" \
  --region $S3_REGION \
  --acl public-read

# Result images
echo "📦 결과 이미지 업로드 중..."
aws s3 sync "$LOCAL_DIR/result/" "s3://$S3_BUCKET/fitting/result/" \
  --region $S3_REGION \
  --acl public-read

echo "✅ 마이그레이션 완료!"
echo ""
echo "DB URL 업데이트 SQL 실행:"
echo "UPDATE virtual_fitting SET"
echo "  user_image_url = REPLACE(user_image_url, '/images/fitting/', 'https://lookfit-fitting-images.s3.ap-northeast-2.amazonaws.com/fitting/'),"
echo "  result_image_url = REPLACE(result_image_url, '/images/fitting/', 'https://lookfit-fitting-images.s3.ap-northeast-2.amazonaws.com/fitting/');"
```

### 5.2. DB URL 업데이트 SQL

```sql
-- S3 마이그레이션 후 실행
UPDATE virtual_fitting
SET
  user_image_url = REPLACE(
    user_image_url,
    '/images/fitting/',
    'https://lookfit-fitting-images.s3.ap-northeast-2.amazonaws.com/fitting/'
  ),
  result_image_url = REPLACE(
    result_image_url,
    '/images/fitting/',
    'https://lookfit-fitting-images.s3.ap-northeast-2.amazonaws.com/fitting/'
  )
WHERE user_image_url LIKE '/images/fitting/%';
```

---

## 📋 전체 일정 (5-6일)

| Phase | 작업 | 소요 시간 |
|-------|------|----------|
| Phase 1 | Hugging Face API 설정, 로컬 디렉토리 생성 | 0.5일 |
| Phase 2 | Entity, Repository, SQL 생성 | 0.5일 |
| Phase 3 | Service, Controller 구현 | 2-3일 |
| Phase 4 | VirtualFitting 컴포넌트, CSS | 2일 |
| Phase 5 | 테스트 및 버그 수정 | 1일 |

**총 예상 기간**: 5-6일

---

## ⚠️ 주의사항

### Hugging Face API 제약사항
1. **Rate Limit**: 시간당 요청 수 제한 (무료 티어)
2. **처리 속도**: 1-2분 소요 (유료 API 대비 느림)
3. **동시 처리**: 1개씩만 처리 (큐 대기)
4. **품질**: Replicate 대비 다소 낮을 수 있음

### 대안 (나중에 고려)
- **사용량 증가 시**: Replicate 유료 API로 전환 (빠른 속도)
- **완전 무료 유지**: 로컬 GPU 서버 + Stable Diffusion 구축

---

## 다음 단계

1. **Hugging Face 가입** 및 API 토큰 발급
2. **DB 테이블 생성** (virtual_fitting)
3. **로컬 이미지 디렉토리** 생성
4. **백엔드 구현** 시작 (Entity → Service → Controller)
5. **프론트엔드 구현** (VirtualFitting 컴포넌트)

---

이 계획대로 진행하시겠습니까?
