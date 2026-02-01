package com.lookfit.cart.dto;

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
        private String pID;

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
        private String pID;
        private String pname;
        private Integer amount;
        private BigDecimal pprice;
        private BigDecimal subtotal;
        private String imageUrl;

        public static ItemResponse from(Cart cart) {
            BigDecimal subtotal = cart.getPprice().multiply(BigDecimal.valueOf(cart.getAmount()));
            return ItemResponse.builder()
                    .pID(cart.getPID())
                    .pname(cart.getPname())
                    .amount(cart.getAmount())
                    .pprice(cart.getPprice())
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
