package com.lookfit.product.service;

import com.lookfit.global.exception.BusinessException;
import com.lookfit.global.exception.ErrorCode;
import com.lookfit.order.repository.OrderItemRepository;
import com.lookfit.order.repository.OrderRepository;
import com.lookfit.product.domain.Review;
import com.lookfit.product.dto.ReviewDto;
import com.lookfit.product.repository.ProductRepository;
import com.lookfit.product.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Value("${review.image.upload-dir:src/main/resources/static/images/reviews}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 리뷰 작성
     * - 상품 존재 확인
     * - 구매 여부 확인
     * - 중복 리뷰 확인
     * - 이미지 업로드 (선택)
     */
    @Transactional
    public ReviewDto.Response createReview(String memberId, String productId,
                                           ReviewDto.CreateRequest request,
                                           MultipartFile image) {
        // 1. 상품 존재 확인
        if (!productRepository.existsById(productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 2. 구매 여부 확인
        if (!hasPurchased(memberId, productId)) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_PURCHASED, "해당 상품을 구매한 사용자만 리뷰를 작성할 수 있습니다");
        }

        // 3. 중복 리뷰 확인
        if (reviewRepository.existsByProductIdAndMemberIdAndDeletedAtIsNull(productId, memberId)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS, "이미 해당 상품에 리뷰를 작성했습니다");
        }

        // 4. 이미지 업로드
        String imageUrl = null;
        String originalFilename = null;
        if (image != null && !image.isEmpty()) {
            validateImageFile(image);
            originalFilename = image.getOriginalFilename();
            imageUrl = saveImage(image, productId);
        }

        // 5. 리뷰 저장
        Review review = Review.builder()
                .productId(productId)
                .memberId(memberId)
                .rating(request.getRating())
                .content(escapeHtml(request.getContent()))
                .imageUrl(imageUrl)
                .originalFilename(originalFilename)
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("리뷰 작성 완료 - reviewId: {}, productId: {}, memberId: {}", savedReview.getReviewId(), productId, memberId);

        return ReviewDto.Response.from(savedReview, memberId);
    }

    /**
     * 상품별 리뷰 목록 조회 (페이징, 최신순)
     */
    public ReviewDto.ReviewPage getReviews(String productId, int page, int size, String currentMemberId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage = reviewRepository.findByProductIdAndDeletedAtIsNull(productId, pageable);

        List<ReviewDto.Response> reviews = reviewPage.getContent().stream()
                .map(review -> ReviewDto.Response.from(review, currentMemberId))
                .toList();

        return ReviewDto.ReviewPage.builder()
                .reviews(reviews)
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .currentPage(page)
                .hasNext(reviewPage.hasNext())
                .hasPrevious(reviewPage.hasPrevious())
                .build();
    }

    /**
     * 리뷰 요약 (평균 별점 + 리뷰 수)
     */
    public ReviewDto.ReviewSummary getReviewSummary(String productId) {
        Double averageRating = reviewRepository.getAverageRatingByProductId(productId);
        Long reviewCount = reviewRepository.countByProductIdAndNotDeleted(productId);

        // 평균 별점은 소수점 첫째 자리까지
        double roundedAverage = Math.round(averageRating * 10.0) / 10.0;

        return ReviewDto.ReviewSummary.builder()
                .averageRating(roundedAverage)
                .reviewCount(reviewCount)
                .build();
    }

    /**
     * 리뷰 수정 (본인만)
     */
    @Transactional
    public ReviewDto.Response updateReview(String memberId, Long reviewId,
                                           ReviewDto.UpdateRequest request,
                                           MultipartFile image) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if (review.isDeleted()) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }

        // 본인 확인
        if (!review.isOwner(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인의 리뷰만 수정할 수 있습니다");
        }

        // 별점/내용 수정
        Integer newRating = request.getRating() != null ? request.getRating() : review.getRating();
        String newContent = request.getContent() != null ? escapeHtml(request.getContent()) : review.getContent();
        review.update(newRating, newContent);

        // 이미지 수정 (새 이미지가 있으면 교체)
        if (image != null && !image.isEmpty()) {
            validateImageFile(image);
            String imageUrl = saveImage(image, review.getProductId());
            review.updateImage(imageUrl, image.getOriginalFilename());
        }

        Review savedReview = reviewRepository.save(review);
        log.info("리뷰 수정 완료 - reviewId: {}, memberId: {}", reviewId, memberId);

        return ReviewDto.Response.from(savedReview, memberId);
    }

    /**
     * 리뷰 삭제 (본인만, Soft delete)
     */
    @Transactional
    public void deleteReview(String memberId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if (review.isDeleted()) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }

        // 본인 확인
        if (!review.isOwner(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인의 리뷰만 삭제할 수 있습니다");
        }

        review.softDelete();
        reviewRepository.save(review);
        log.info("리뷰 삭제 완료 (soft) - reviewId: {}, memberId: {}", reviewId, memberId);
    }

    /**
     * 구매 여부 확인
     * Buy 테이블에서 해당 회원의 주문을 찾고, OrderItem에서 해당 상품이 있는지 확인
     */
    private boolean hasPurchased(String memberId, String productId) {
        // 해당 회원의 모든 주문에서 해당 상품이 포함된 주문 아이템이 있는지 확인
        return orderRepository.findByMemberid(memberId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent()
                .stream()
                .anyMatch(buy -> orderItemRepository.findByOrderno(buy.getOrderno())
                        .stream()
                        .anyMatch(item -> item.getProductId().equals(productId)));
    }

    /**
     * 이미지 파일 검증
     */
    private void validateImageFile(MultipartFile file) {
        // 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미지 파일 크기는 5MB 이하여야 합니다");
        }

        // 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "파일명이 없습니다");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "허용되지 않는 파일 형식입니다. 허용: jpg, jpeg, png, webp");
        }

        // Content-Type 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미지 파일만 업로드 가능합니다");
        }
    }

    /**
     * 이미지 저장 (로컬 파일 시스템)
     */
    private String saveImage(MultipartFile file, String productId) {
        try {
            Path uploadPath = Paths.get(uploadDir, productId);
            Files.createDirectories(uploadPath);

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String newFilename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.debug("이미지 저장 완료 - path: {}", filePath);
            return "/images/reviews/" + productId + "/" + newFilename;
        } catch (IOException e) {
            log.error("이미지 저장 실패", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * XSS 방지 - HTML 태그 이스케이프
     */
    private String escapeHtml(String input) {
        if (input == null) return null;
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
