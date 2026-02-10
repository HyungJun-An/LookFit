package com.lookfit.fitting.service;

import com.lookfit.fitting.domain.FittingStatus;
import com.lookfit.fitting.domain.VirtualFitting;
import com.lookfit.fitting.dto.FittingDto;
import com.lookfit.fitting.repository.VirtualFittingRepository;
import com.lookfit.global.exception.BusinessException;
import com.lookfit.global.exception.ErrorCode;
import com.lookfit.product.domain.Product;
import com.lookfit.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 가상 피팅 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VirtualFittingService {

    private final VirtualFittingRepository fittingRepository;
    private final ProductRepository productRepository;
    private final HuggingFaceGradioService huggingFaceGradioService;

    @Value("${fitting.image.upload-dir:src/main/resources/static/images/fitting/user}")
    private String uploadDir;

    @Value("${fitting.image.result-dir:src/main/resources/static/images/fitting/result}")
    private String resultDir;

    @Value("${fitting.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Priority A: 사용자 이미지 업로드
     *
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @param category 카테고리 (upper_body, lower_body, dresses)
     * @param imageFile 업로드할 이미지 파일
     * @return 업로드 응답
     */
    @Transactional
    public FittingDto.UploadResponse uploadUserImage(
            String memberId,
            String productId,
            String category,
            MultipartFile imageFile
    ) {
        log.info("사용자 이미지 업로드 시작 - memberId: {}, productId: {}, category: {}",
                memberId, productId, category);

        // 1. 유효성 검증
        validateUploadRequest(productId, category, imageFile);

        // 2. 피팅 ID 생성 (UUID)
        String fittingId = UUID.randomUUID().toString();

        // 3. 이미지 파일 저장
        String imageUrl = saveImageFile(memberId, fittingId, imageFile);
        log.info("이미지 저장 완료 - imageUrl: {}", imageUrl);

        // 4. VirtualFitting 엔티티 생성
        VirtualFitting fitting = VirtualFitting.builder()
                .fittingId(fittingId)
                .memberId(memberId)
                .productId(productId)
                .userImageUrl(imageUrl)
                .category(category)
                .status(FittingStatus.PENDING)
                .build();

        // 5. DB 저장
        VirtualFitting savedFitting = fittingRepository.save(fitting);
        log.info("가상 피팅 생성 완료 - fittingId: {}", fittingId);

        return FittingDto.UploadResponse.from(savedFitting);
    }

    /**
     * 업로드 요청 유효성 검증
     */
    private void validateUploadRequest(String productId, String category, MultipartFile imageFile) {
        // 상품 존재 여부 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 카테고리 유효성 검증
        if (!isValidCategory(category)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "유효하지 않은 카테고리입니다. (upper_body, lower_body, dresses 중 선택)");
        }

        // 이미지 파일 검증
        if (imageFile == null || imageFile.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미지 파일이 비어있습니다.");
        }

        // 파일 크기 검증 (최대 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (imageFile.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "이미지 파일 크기는 10MB를 초과할 수 없습니다.");
        }

        // 파일 확장자 검증
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "이미지 파일만 업로드 가능합니다.");
        }

        log.debug("업로드 요청 유효성 검증 완료 - productId: {}, category: {}, fileSize: {}",
                productId, category, imageFile.getSize());
    }

    /**
     * 카테고리 유효성 확인
     */
    private boolean isValidCategory(String category) {
        return "upper_body".equals(category) ||
               "lower_body".equals(category) ||
               "dresses".equals(category);
    }

    /**
     * 이미지 파일 로컬 저장
     *
     * @return 저장된 이미지 URL (상대 경로)
     */
    private String saveImageFile(String memberId, String fittingId, MultipartFile imageFile) {
        try {
            // 파일명 생성: {fittingId}.jpg (일단 .jpg로 저장)
            String filename = fittingId + ".jpg";

            // 절대 경로로 저장 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir, memberId).toAbsolutePath();
            Files.createDirectories(uploadPath);
            log.debug("업로드 디렉토리 생성: {}", uploadPath);

            // 파일 저장 (원본 그대로)
            Path filePath = uploadPath.resolve(filename);
            imageFile.transferTo(filePath.toFile());
            log.info("이미지 파일 저장 완료 - path: {}", filePath);

            // 상대 경로 반환 (프론트엔드에서 사용)
            String relativeUrl = "/images/fitting/user/" + memberId + "/" + filename;
            log.info("이미지 URL: {}", relativeUrl);

            return relativeUrl;

        } catch (IOException e) {
            log.error("이미지 파일 저장 실패 - memberId: {}, fittingId: {}", memberId, fittingId, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED,
                    "이미지 파일 저장에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * Priority B: AI 가상 피팅 이미지 생성 요청
     *
     * @param fittingId 피팅 ID
     * @return 생성 응답
     */
    @Transactional
    public FittingDto.GenerateResponse generateFitting(String fittingId) {
        log.info("AI 피팅 생성 요청 - fittingId: {}", fittingId);

        // 1. 피팅 조회
        VirtualFitting fitting = fittingRepository.findById(fittingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FITTING_NOT_FOUND));

        // 2. 상태 확인 (PENDING만 생성 가능)
        if (fitting.getStatus() != FittingStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "이미 처리 중이거나 완료된 피팅입니다. 현재 상태: " + fitting.getStatus());
        }

        // 3. 상품 조회 (의류 이미지 URL 가져오기)
        Product product = productRepository.findById(fitting.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 4. Hugging Face Gradio API 호출
        try {
            // 상대 경로 그대로 전달 (HuggingFaceGradioService에서 로컬 경로로 변환)
            String userImageUrl = fitting.getUserImageUrl();
            String garmentImageUrl = product.getImageUrl();

            log.info("🤗 Hugging Face Gradio API 호출 - user: {}, garment: {}", userImageUrl, garmentImageUrl);

            String resultImageUrl = huggingFaceGradioService.generateVirtualTryOn(
                    userImageUrl,
                    garmentImageUrl,
                    fitting.getCategory()
            );

            // 5. 상태 업데이트 (즉시 COMPLETED)
            fitting.startProcessing("hf_gradio_" + System.currentTimeMillis());
            fitting.complete(resultImageUrl);
            fittingRepository.save(fitting);

            log.info("✅ Hugging Face Gradio AI 완료 - fittingId: {}, resultUrl: {}", fittingId, resultImageUrl);
            return FittingDto.GenerateResponse.from(fitting);

        } catch (BusinessException e) {
            // GPU 할당량 초과 등의 비즈니스 예외는 그대로 전파
            log.error("❌ Hugging Face Gradio 실패 - fittingId: {}, error: {}", fittingId, e.getErrorCode());
            fitting.fail("AI 생성 실패: " + e.getMessage());
            fittingRepository.save(fitting);
            throw e;
        } catch (Exception e) {
            log.error("❌ Hugging Face Gradio 실패 - fittingId: {}", fittingId, e);
            fitting.fail("AI 생성 실패: " + e.getMessage());
            fittingRepository.save(fitting);
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED,
                    "AI 이미지 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * Priority B: 피팅 상태 조회 (폴링용)
     *
     * @param fittingId 피팅 ID
     * @return 상태 응답
     */
    @Transactional(readOnly = true)
    public FittingDto.StatusResponse getFittingStatus(String fittingId) {
        log.debug("피팅 상태 조회 - fittingId: {}", fittingId);

        VirtualFitting fitting = fittingRepository.findById(fittingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FITTING_NOT_FOUND));

        // Hugging Face는 동기 방식 - 즉시 완료
        return FittingDto.StatusResponse.from(fitting);
    }

    /**
     * Priority C: 피팅 기록 조회 (페이징)
     *
     * @param memberId 회원 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 피팅 기록 목록
     */
    public FittingDto.HistoryResponse getFittingHistory(String memberId, int page, int size) {
        log.debug("피팅 기록 조회 - memberId: {}, page: {}, size: {}", memberId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<VirtualFitting> fittingPage = fittingRepository.findByMemberIdOrderByCreatedAtDesc(
                memberId,
                pageable
        );

        List<FittingDto.DetailResponse> fittings = fittingPage.getContent().stream()
                .map(FittingDto.DetailResponse::from)
                .toList();

        return FittingDto.HistoryResponse.builder()
                .fittings(fittings)
                .totalCount(fittingPage.getTotalElements())
                .page(page)
                .pageSize(size)
                .totalPages(fittingPage.getTotalPages())
                .build();
    }

    /**
     * Priority C: 피팅 상세 정보 조회
     *
     * @param fittingId 피팅 ID
     * @param memberId 회원 ID (권한 확인용)
     * @return 피팅 상세 정보
     */
    public FittingDto.DetailResponse getFittingDetail(String fittingId, String memberId) {
        log.debug("피팅 상세 조회 - fittingId: {}, memberId: {}", fittingId, memberId);

        VirtualFitting fitting = fittingRepository.findById(fittingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FITTING_NOT_FOUND));

        // 권한 확인 (본인의 피팅만 조회 가능)
        if (!fitting.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "다른 사용자의 피팅 정보는 조회할 수 없습니다.");
        }

        return FittingDto.DetailResponse.from(fitting);
    }
}
