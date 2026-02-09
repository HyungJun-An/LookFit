package com.lookfit.wishlist.controller;

import com.lookfit.wishlist.dto.WishlistDto;
import com.lookfit.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 찜하기 REST API
 * 모든 엔드포인트는 인증 필요 (Spring Security에서 처리)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * GET /api/v1/wishlist - 찜 목록 조회
     */
    @GetMapping
    public ResponseEntity<WishlistDto.ListResponse> getWishlist(
            @AuthenticationPrincipal String memberId) {

        WishlistDto.ListResponse response = wishlistService.getWishlist(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/wishlist - 찜 추가
     */
    @PostMapping
    public ResponseEntity<Void> addToWishlist(
            @AuthenticationPrincipal String memberId,
            @RequestBody WishlistDto.AddRequest request) {

        wishlistService.addToWishlist(memberId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/v1/wishlist/{productId} - 찜 삭제
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            @AuthenticationPrincipal String memberId,
            @PathVariable String productId) {

        wishlistService.removeFromWishlist(memberId, productId);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/v1/wishlist/status/{productId} - 특정 상품 찜 상태 확인
     */
    @GetMapping("/status/{productId}")
    public ResponseEntity<WishlistDto.StatusResponse> checkStatus(
            @AuthenticationPrincipal String memberId,
            @PathVariable String productId) {

        WishlistDto.StatusResponse response = wishlistService.checkWishlistStatus(memberId, productId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/wishlist/count - 찜 개수 조회
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getWishlistCount(
            @AuthenticationPrincipal String memberId) {

        long count = wishlistService.getWishlistCount(memberId);
        return ResponseEntity.ok(count);
    }
}
