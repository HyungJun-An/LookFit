package com.lookfit.wishlist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lookfit.product.domain.Product;
import com.lookfit.wishlist.domain.Wishlist;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class WishlistDto {

    /**
     * 찜 추가 요청
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddRequest {
        @JsonProperty("productId")
        private String productId;
    }

    /**
     * 찜 삭제 요청
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemoveRequest {
        @JsonProperty("productId")
        private String productId;
    }

    /**
     * 찜 항목 응답
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResponse {
        @JsonProperty("productId")
        private String productId;

        @JsonProperty("productName")
        private String productName;

        @JsonProperty("productPrice")
        private BigDecimal productPrice;

        @JsonProperty("productCategory")
        private String productCategory;

        private String imageUrl;

        @JsonProperty("productStock")
        private Integer productStock;

        private LocalDateTime addedAt;

        /**
         * Wishlist를 ItemResponse로 변환
         */
        public static ItemResponse from(Wishlist wishlist) {
            Product product = wishlist.getProduct();

            return ItemResponse.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .productPrice(product.getProductPrice())
                    .productCategory(product.getProductCategory())
                    .imageUrl(product.getImageUrl())
                    .productStock(product.getProductStock())
                    .addedAt(wishlist.getAddedAt())
                    .build();
        }
    }

    /**
     * 찜 목록 응답
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private List<ItemResponse> items;
        private int totalCount;

        /**
         * Wishlist 리스트를 ListResponse로 변환
         */
        public static ListResponse from(List<Wishlist> wishlists) {
            List<ItemResponse> items = wishlists.stream()
                    .map(ItemResponse::from)
                    .collect(Collectors.toList());

            return ListResponse.builder()
                    .items(items)
                    .totalCount(items.size())
                    .build();
        }
    }

    /**
     * 찜 상태 확인 응답
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusResponse {
        @JsonProperty("productId")
        private String productId;
        private boolean isWished;
        private long wishCount;  // 해당 상품의 전체 찜 개수
    }
}
