package com.lookfit.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lookfit.product.domain.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

public class ProductDto {

    @Getter
    @Builder
    public static class Response {
        @JsonProperty("productId")
        private String productId;

        @JsonProperty("productName")
        private String productName;

        @JsonProperty("productPrice")
        private BigDecimal productPrice;

        @JsonProperty("productCategory")
        private String productCategory;

        @JsonProperty("description")
        private String description;

        @JsonProperty("productCompany")
        private String productCompany;

        @JsonProperty("productStock")
        private Integer productStock;

        @JsonProperty("imageUrl")
        private String imageUrl;

        public static Response from(Product product) {
            return Response.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .productPrice(product.getProductPrice())
                    .productCategory(product.getProductCategory())
                    .description(product.getDescription())
                    .productCompany(product.getProductCompany())
                    .productStock(product.getProductStock())
                    .imageUrl(product.getImageUrl())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ListResponse {
        private java.util.List<Response> content;
        private long totalElements;
        private int totalPages;
        private int currentPage;
    }
}
