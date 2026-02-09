package com.lookfit.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lookfit.cart.domain.Cart;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

public class CartDto {

    @Getter
    @Setter
    public static class AddRequest {
        @NotBlank(message = "상품 ID는 필수입니다")
        @JsonProperty("productId")
        private String productId;

        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        private Integer amount = 1;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        private Integer amount;
    }

    @Getter
    @Builder
    public static class ItemResponse {
        @JsonProperty("productId")
        private String productId;

        @JsonProperty("productName")
        private String productName;

        @JsonProperty("amount")
        private Integer amount;

        @JsonProperty("productPrice")
        private BigDecimal productPrice;

        @JsonProperty("subtotal")
        private BigDecimal subtotal;

        @JsonProperty("imageUrl")
        private String imageUrl;

        public static ItemResponse from(Cart cart) {
            BigDecimal subtotal = cart.getProductPrice().multiply(BigDecimal.valueOf(cart.getAmount()));
            return ItemResponse.builder()
                    .productId(cart.getProductId())
                    .productName(cart.getProductName())
                    .amount(cart.getAmount())
                    .productPrice(cart.getProductPrice())
                    .subtotal(subtotal)
                    .imageUrl(cart.getImageUrl())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ListResponse {
        private List<ItemResponse> items;
        private int totalAmount;
        private BigDecimal totalPrice;
    }
}
