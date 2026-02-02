package com.lookfit.product.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Product 도메인 이벤트 정의
 */
public class ProductEvents {

    /**
     * 상품 생성 이벤트
     */
    @Getter
    @RequiredArgsConstructor
    public static class ProductCreatedEvent {
        private final String productId;
    }

    /**
     * 상품 수정 이벤트
     */
    @Getter
    @RequiredArgsConstructor
    public static class ProductUpdatedEvent {
        private final String productId;
    }

    /**
     * 상품 삭제 이벤트
     */
    @Getter
    @RequiredArgsConstructor
    public static class ProductDeletedEvent {
        private final String productId;
    }

    /**
     * 상품 재고 변경 이벤트
     */
    @Getter
    @RequiredArgsConstructor
    public static class ProductStockChangedEvent {
        private final String productId;
        private final int oldStock;
        private final int newStock;
    }
}
