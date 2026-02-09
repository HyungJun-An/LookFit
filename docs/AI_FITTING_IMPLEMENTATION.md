# AI 가상 피팅 구현 계획

## 1. Replicate API 설정

### 가입 및 API 키 발급
1. https://replicate.com 가입
2. Account Settings > API Tokens에서 API 키 발급
3. 무료 크레딧: 처음 가입 시 제공

### 사용할 모델
**IDM-VTON** (Improved Diffusion Models for Virtual Try-On)
- 모델 ID: `cuuupid/idm-vton`
- GitHub: https://github.com/yisol/IDM-VTON
- 입력:
  - `human_img`: 사용자 전신 사진 (PNG/JPG)
  - `garment_img`: 상품 이미지 (PNG/JPG)
  - `category`: "upper_body" (상의) / "lower_body" (하의) / "dresses" (원피스)
- 출력: 합성된 가상 피팅 이미지

### 비용 예상
- 이미지 1장 생성: 약 10-20초 → $0.005-0.01
- 월 100회 사용 시: $0.5-1.0

---

## 2. 백엔드 구현

### 2.1. Entity 생성

```java
// VirtualFitting.java
@Entity
@Table(name = "virtual_fitting")
public class VirtualFitting {
    @Id
    @Column(name = "fitting_id", length = 36)
    private String fittingId; // UUID

    @Column(name = "member_id", length = 100, nullable = false)
    private String memberId;

    @Column(name = "product_id", length = 30, nullable = false)
    private String productId;

    @Column(name = "user_image_url", length = 500)
    private String userImageUrl; // S3에 업로드된 사용자 사진

    @Column(name = "result_image_url", length = 500)
    private String resultImageUrl; // 생성된 피팅 이미지

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private FittingStatus status; // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(name = "replicate_prediction_id", length = 100)
    private String replicatePredictionId; // Replicate 작업 ID

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}

enum FittingStatus {
    PENDING,      // 요청 생성됨
    PROCESSING,   // AI 생성 중
    COMPLETED,    // 완료
    FAILED        // 실패
}
```

### 2.2. API 엔드포인트

```java
// VirtualFittingController.java
@RestController
@RequestMapping("/api/v1/fitting")
public class VirtualFittingController {

    // 1. 사용자 이미지 업로드
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadUserImage(
        @RequestParam("image") MultipartFile image
    ) {
        // S3에 이미지 업로드
        // 업로드된 URL 반환
    }

    // 2. AI 피팅 생성 요청
    @PostMapping("/generate")
    public ResponseEntity<FittingResponse> generateFitting(
        @RequestBody FittingRequest request
    ) {
        // 1. VirtualFitting 엔티티 생성 (status: PENDING)
        // 2. Replicate API 호출 (비동기)
        // 3. fittingId 즉시 반환
    }

    // 3. 피팅 결과 조회 (폴링용)
    @GetMapping("/{fittingId}")
    public ResponseEntity<FittingResponse> getFitting(
        @PathVariable String fittingId
    ) {
        // status, resultImageUrl 반환
    }

    // 4. 내 피팅 기록
    @GetMapping("/history")
    public ResponseEntity<List<FittingResponse>> getMyFittings() {
        // 로그인한 사용자의 피팅 기록
    }

    // 5. Replicate Webhook (내부용)
    @PostMapping("/webhook")
    public ResponseEntity<Void> replicateWebhook(
        @RequestBody ReplicateWebhookPayload payload
    ) {
        // Replicate에서 완료 시 호출
        // status를 COMPLETED로 업데이트
    }
}
```

### 2.3. Service Layer

```java
// VirtualFittingService.java
@Service
public class VirtualFittingService {

    @Value("${replicate.api.key}")
    private String replicateApiKey;

    @Async
    public CompletableFuture<String> generateFitting(
        String fittingId,
        String userImageUrl,
        String productImageUrl,
        String category
    ) {
        // 1. Replicate API 호출
        String predictionId = callReplicateAPI(userImageUrl, productImageUrl, category);

        // 2. DB 업데이트 (status: PROCESSING, predictionId 저장)
        updateFittingStatus(fittingId, FittingStatus.PROCESSING, predictionId);

        // 3. 폴링 또는 Webhook 대기
        return CompletableFuture.completedFuture(predictionId);
    }

    private String callReplicateAPI(String humanImg, String garmentImg, String category) {
        // Replicate API 호출
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> input = Map.of(
            "human_img", humanImg,
            "garment_img", garmentImg,
            "category", category
        );

        Map<String, Object> request = Map.of(
            "version", "c871bb9b046607b680449ecbae55fd8c6d945e0a1948644bf2361b3d021d3ff4",
            "input", input,
            "webhook", "https://your-domain.com/api/v1/fitting/webhook"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + replicateApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "https://api.replicate.com/v1/predictions",
            entity,
            Map.class
        );

        return (String) response.getBody().get("id");
    }
}
```

### 2.4. application.yml 설정

```yaml
# Replicate API 설정
replicate:
  api:
    key: ${REPLICATE_API_KEY} # 환경변수로 관리

# S3 설정 (이미지 저장용)
cloud:
  aws:
    s3:
      bucket: lookfit-fitting-images
      region: ap-northeast-2
```

---

## 3. 프론트엔드 구현

### 3.1. VirtualFitting 컴포넌트 개선

```typescript
// VirtualFitting.tsx
const VirtualFitting = () => {
  const [userImage, setUserImage] = useState<File | null>(null);
  const [userImagePreview, setUserImagePreview] = useState<string>('');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [fittingId, setFittingId] = useState<string | null>(null);
  const [status, setStatus] = useState<'idle' | 'uploading' | 'processing' | 'completed'>('idle');
  const [resultImageUrl, setResultImageUrl] = useState<string>('');

  // 1. 사용자 이미지 업로드
  const handleImageUpload = async (file: File) => {
    const formData = new FormData();
    formData.append('image', file);

    const response = await axiosInstance.post('/api/v1/fitting/upload', formData);
    return response.data.imageUrl;
  };

  // 2. AI 피팅 생성
  const handleGenerateFitting = async () => {
    setStatus('uploading');

    // 이미지 업로드
    const userImageUrl = await handleImageUpload(userImage!);

    // AI 피팅 요청
    setStatus('processing');
    const response = await axiosInstance.post('/api/v1/fitting/generate', {
      productId: selectedProduct!.productId,
      userImageUrl: userImageUrl,
      category: selectedProduct!.productCategory
    });

    setFittingId(response.data.fittingId);

    // 폴링으로 결과 확인 (3초마다)
    const interval = setInterval(async () => {
      const result = await axiosInstance.get(`/api/v1/fitting/${response.data.fittingId}`);

      if (result.data.status === 'COMPLETED') {
        setResultImageUrl(result.data.resultImageUrl);
        setStatus('completed');
        clearInterval(interval);
      } else if (result.data.status === 'FAILED') {
        alert('AI 피팅 생성에 실패했습니다.');
        setStatus('idle');
        clearInterval(interval);
      }
    }, 3000);
  };

  return (
    <div className="virtual-fitting">
      {/* Step 1: 사용자 이미지 업로드 */}
      <div className="upload-section">
        <h2>내 사진 업로드</h2>
        <input type="file" accept="image/*" onChange={(e) => {
          const file = e.target.files?.[0];
          if (file) {
            setUserImage(file);
            setUserImagePreview(URL.createObjectURL(file));
          }
        }} />
        {userImagePreview && <img src={userImagePreview} alt="Preview" />}
      </div>

      {/* Step 2: 상품 선택 */}
      <div className="product-selection">
        <h2>입어볼 상품 선택</h2>
        {/* 상품 목록 표시 */}
      </div>

      {/* Step 3: AI 피팅 생성 버튼 */}
      <button
        disabled={!userImage || !selectedProduct || status !== 'idle'}
        onClick={handleGenerateFitting}
      >
        {status === 'processing' ? 'AI 생성 중...' : 'AI 피팅 해보기'}
      </button>

      {/* Step 4: 결과 표시 */}
      {status === 'completed' && (
        <div className="result-section">
          <h2>AI 피팅 결과</h2>
          <img src={resultImageUrl} alt="Fitting Result" />
        </div>
      )}

      {/* Loading Spinner */}
      {status === 'processing' && (
        <div className="loading-spinner">
          <p>AI가 가상 피팅 이미지를 생성하고 있습니다...</p>
          <p>약 10-30초 소요됩니다.</p>
        </div>
      )}
    </div>
  );
};
```

---

## 4. 데이터 흐름

```
1. 사용자가 자신의 전신 사진 업로드
   ↓
2. 프론트엔드 → POST /api/v1/fitting/upload
   ↓
3. 백엔드가 S3에 이미지 저장 → URL 반환
   ↓
4. 사용자가 상품 선택 후 "AI 피팅 해보기" 클릭
   ↓
5. 프론트엔드 → POST /api/v1/fitting/generate
   {
     productId: "P001",
     userImageUrl: "https://s3.../user-photo.jpg",
     category: "상의"
   }
   ↓
6. 백엔드:
   - VirtualFitting 엔티티 생성 (status: PENDING)
   - Replicate API 호출 (비동기)
   - fittingId 즉시 반환
   ↓
7. 프론트엔드: 3초마다 GET /api/v1/fitting/{fittingId} 폴링
   ↓
8. Replicate가 이미지 생성 완료 (10-30초 후)
   ↓
9. Replicate Webhook → POST /api/v1/fitting/webhook
   ↓
10. 백엔드:
    - status → COMPLETED
    - resultImageUrl 저장
    ↓
11. 프론트엔드 폴링이 COMPLETED 감지
    ↓
12. 결과 이미지 표시
```

---

## 5. 다음 단계

### 즉시 시작 가능:
1. Replicate 가입 및 무료 크레딧 확인
2. 백엔드 Entity + Repository 생성
3. 이미지 업로드 API 구현 (S3 연동)

### 질문:
1. **Replicate 무료 크레딧으로 시작**하시겠습니까? (추천)
2. 아니면 **완전 무료 (Hugging Face)** 로 가시겠습니까? (느릴 수 있음)
3. **이미지 저장 위치**: S3 vs 로컬 서버?

어떤 방식으로 진행할까요?
