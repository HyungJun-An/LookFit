package com.lookfit.fitting.controller;

import com.lookfit.fitting.dto.FittingDto;
import com.lookfit.fitting.service.VirtualFittingService;
import com.lookfit.global.exception.BusinessException;
import com.lookfit.global.exception.ErrorCode;
import com.lookfit.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 가상 피팅 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/fitting")
@RequiredArgsConstructor
public class VirtualFittingController {

    private final VirtualFittingService fittingService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Priority A: 사용자 이미지 업로드
     *
     * POST /api/v1/fitting/upload
     * Content-Type: multipart/form-data
     *
     * @param productId 상품 ID (form-data)
     * @param category 카테고리 (form-data): upper_body, lower_body, dresses
     * @param image 업로드할 이미지 파일 (form-data)
     * @param authorizationHeader JWT 토큰 (Authorization: Bearer {token})
     * @return 업로드 응답
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FittingDto.UploadResponse> uploadUserImage(
            @RequestParam("productId") String productId,
            @RequestParam("category") String category,
            @RequestParam("image") MultipartFile image,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        log.info("사용자 이미지 업로드 요청 - productId: {}, category: {}", productId, category);

        // JWT 토큰에서 memberId 추출
        String memberId = extractMemberIdFromToken(authorizationHeader);

        // 업로드 처리
        FittingDto.UploadResponse response = fittingService.uploadUserImage(
                memberId,
                productId,
                category,
                image
        );

        log.info("이미지 업로드 완료 - fittingId: {}", response.getFittingId());
        return ResponseEntity.ok(response);
    }

    /**
     * JWT 토큰에서 memberId 추출
     * 테스트용 고정 토큰 지원
     */
    private String extractMemberIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 토큰이 필요합니다.");
        }

        String token = authorizationHeader.substring(7);

        // 테스트용 고정 토큰 체크
        if ("test_token_fitting".equals(token)) {
            log.warn("⚠️ 테스트용 고정 토큰 사용 - memberId: google_11510");
            return "google_11510";
        }

        try {
            // 실제 JWT 토큰 파싱
            String memberId = jwtTokenProvider.getMemberId(token);
            log.debug("JWT 토큰에서 memberId 추출 성공: {}", memberId);
            return memberId;
        } catch (Exception e) {
            log.error("JWT 토큰 파싱 실패", e);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
    }

    /**
     * Priority B: AI 가상 피팅 이미지 생성 요청
     *
     * POST /api/v1/fitting/generate
     *
     * @param request 생성 요청 (fittingId)
     * @return 생성 응답
     */
    @PostMapping("/generate")
    public ResponseEntity<FittingDto.GenerateResponse> generateFitting(
            @RequestBody FittingDto.GenerateRequest request
    ) {
        log.info("AI 피팅 생성 요청 - fittingId: {}", request.getFittingId());

        FittingDto.GenerateResponse response = fittingService.generateFitting(request.getFittingId());

        log.info("AI 피팅 생성 요청 완료 - fittingId: {}, predictionId: {}",
                response.getFittingId(), response.getReplicatePredictionId());
        return ResponseEntity.ok(response);
    }

    /**
     * Priority B: 피팅 상태 조회 (폴링용)
     *
     * GET /api/v1/fitting/{fittingId}
     *
     * @param fittingId 피팅 ID
     * @return 상태 응답
     */
    @GetMapping("/{fittingId}")
    public ResponseEntity<FittingDto.StatusResponse> getFittingStatus(
            @PathVariable String fittingId
    ) {
        log.debug("피팅 상태 조회 - fittingId: {}", fittingId);

        FittingDto.StatusResponse response = fittingService.getFittingStatus(fittingId);

        return ResponseEntity.ok(response);
    }

    /**
     * Priority C: 피팅 기록 조회
     *
     * GET /api/v1/fitting/history?page=0&size=10
     *
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @param authorizationHeader JWT 토큰
     * @return 피팅 기록 목록
     */
    @GetMapping("/history")
    public ResponseEntity<FittingDto.HistoryResponse> getFittingHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String memberId = extractMemberIdFromToken(authorizationHeader);
        log.debug("피팅 기록 조회 - memberId: {}, page: {}, size: {}", memberId, page, size);

        FittingDto.HistoryResponse response = fittingService.getFittingHistory(memberId, page, size);

        log.debug("피팅 기록 조회 완료 - memberId: {}, totalCount: {}", memberId, response.getTotalCount());
        return ResponseEntity.ok(response);
    }

    /**
     * Priority C: 피팅 상세 정보 조회
     *
     * GET /api/v1/fitting/{fittingId}/detail
     *
     * @param fittingId 피팅 ID
     * @param authorizationHeader JWT 토큰
     * @return 피팅 상세 정보
     */
    @GetMapping("/{fittingId}/detail")
    public ResponseEntity<FittingDto.DetailResponse> getFittingDetail(
            @PathVariable String fittingId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String memberId = extractMemberIdFromToken(authorizationHeader);
        log.debug("피팅 상세 조회 - fittingId: {}, memberId: {}", fittingId, memberId);

        FittingDto.DetailResponse response = fittingService.getFittingDetail(fittingId, memberId);

        return ResponseEntity.ok(response);
    }
}
