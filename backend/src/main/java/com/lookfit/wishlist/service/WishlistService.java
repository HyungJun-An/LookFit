package com.lookfit.wishlist.service;

import com.lookfit.global.exception.BusinessException;
import com.lookfit.global.exception.ErrorCode;
import com.lookfit.product.domain.Product;
import com.lookfit.product.repository.ProductRepository;
import com.lookfit.wishlist.domain.Wishlist;
import com.lookfit.wishlist.dto.WishlistDto;
import com.lookfit.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 찜하기 서비스
 * 사용자의 찜 목록 관리 (추가, 삭제, 조회, 상태 확인)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    /**
     * 찜 추가
     * - 상품 존재 여부 확인
     * - 중복 추가 방지 (이미 찜한 경우 예외 발생하지 않고 무시)
     */
    @Transactional
    public void addToWishlist(String memberId, WishlistDto.AddRequest request) {
        log.info("Adding product {} to wishlist for user {}", request.getProductId(), memberId);

        // 1. 상품 존재 확인
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 2. 이미 찜한 경우 무시 (중복 추가 방지)
        if (wishlistRepository.existsByMemberIdAndProductId(memberId, request.getProductId())) {
            log.info("Product {} already in wishlist for user {}", request.getProductId(), memberId);
            return;
        }

        // 3. 찜 추가
        Wishlist wishlist = Wishlist.builder()
                .memberId(memberId)
                .productId(request.getProductId())
                .addedAt(LocalDateTime.now())
                .build();

        wishlistRepository.save(wishlist);
        log.info("Successfully added product {} to wishlist for user {}", request.getProductId(), memberId);
    }

    /**
     * 찜 삭제
     */
    @Transactional
    public void removeFromWishlist(String memberId, String productId) {
        log.info("Removing product {} from wishlist for user {}", productId, memberId);

        // 존재하지 않아도 예외 발생하지 않음 (멱등성 보장)
        wishlistRepository.deleteByMemberIdAndProductId(memberId, productId);
        log.info("Successfully removed product {} from wishlist for user {}", productId, memberId);
    }

    /**
     * 사용자의 찜 목록 조회 (Product 정보 포함)
     */
    public WishlistDto.ListResponse getWishlist(String memberId) {
        log.info("Fetching wishlist for user {}", memberId);

        List<Wishlist> wishlists = wishlistRepository.findByMemberIdWithProduct(memberId);
        return WishlistDto.ListResponse.from(wishlists);
    }

    /**
     * 특정 상품의 찜 상태 확인
     * - 현재 사용자가 찜했는지 여부
     * - 해당 상품의 전체 찜 개수
     */
    public WishlistDto.StatusResponse checkWishlistStatus(String memberId, String productId) {
        log.info("Checking wishlist status for product {} and user {}", productId, memberId);

        boolean isWished = wishlistRepository.existsByMemberIdAndProductId(memberId, productId);
        long wishCount = wishlistRepository.countByProductId(productId);

        return WishlistDto.StatusResponse.builder()
                .productId(productId)
                .isWished(isWished)
                .wishCount(wishCount)
                .build();
    }

    /**
     * 사용자의 전체 찜 개수 조회
     */
    public long getWishlistCount(String memberId) {
        return wishlistRepository.countByMemberId(memberId);
    }
}
