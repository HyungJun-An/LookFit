package com.lookfit.product.controller;

import com.lookfit.global.exception.BusinessException;
import com.lookfit.global.exception.ErrorCode;
import com.lookfit.product.dto.ReviewDto;
import com.lookfit.product.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 작성 (인증 필수)
     * POST /api/v1/products/{productId}/reviews
     * Content-Type: multipart/form-data
     */
    @PostMapping(value = "/api/v1/products/{productId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewDto.Response> createReview(
            @PathVariable String productId,
            @Valid @RequestPart("review") ReviewDto.CreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {

        requireAuthentication(userDetails);
        String memberId = userDetails.getUsername();
        ReviewDto.Response response = reviewService.createReview(memberId, productId, request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 상품별 리뷰 목록 조회 (인증 불필요, 페이징)
     * GET /api/v1/products/{productId}/reviews?page=0&size=10
     */
    @GetMapping("/api/v1/products/{productId}/reviews")
    public ResponseEntity<ReviewDto.ReviewPage> getReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        String currentMemberId = userDetails != null ? userDetails.getUsername() : null;
        ReviewDto.ReviewPage response = reviewService.getReviews(productId, page, size, currentMemberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 요약 (인증 불필요 - 평균 별점 + 리뷰 수)
     * GET /api/v1/products/{productId}/reviews/summary
     */
    @GetMapping("/api/v1/products/{productId}/reviews/summary")
    public ResponseEntity<ReviewDto.ReviewSummary> getReviewSummary(@PathVariable String productId) {
        ReviewDto.ReviewSummary response = reviewService.getReviewSummary(productId);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 수정 (인증 필수, 본인만)
     * PATCH /api/v1/reviews/{reviewId}
     * Content-Type: multipart/form-data
     */
    @PatchMapping(value = "/api/v1/reviews/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewDto.Response> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestPart("review") ReviewDto.UpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {

        requireAuthentication(userDetails);
        String memberId = userDetails.getUsername();
        ReviewDto.Response response = reviewService.updateReview(memberId, reviewId, request, image);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 삭제 (인증 필수, 본인만, Soft delete)
     * DELETE /api/v1/reviews/{reviewId}
     */
    @DeleteMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {

        requireAuthentication(userDetails);
        String memberId = userDetails.getUsername();
        reviewService.deleteReview(memberId, reviewId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 인증 필수 확인 헬퍼 메서드
     * products/** 가 permitAll 이므로, 쓰기 작업에서 수동으로 인증 확인
     */
    private void requireAuthentication(UserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
