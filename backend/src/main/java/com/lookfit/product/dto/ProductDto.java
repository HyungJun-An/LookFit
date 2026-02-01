package com.lookfit.product.dto;

import com.lookfit.product.domain.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

public class ProductDto {

    @Getter
    @Builder
    public static class Response {
        private String pID;
        private String pname;
        private BigDecimal pprice;
        private String pcategory;
        private String description;
        private String pcompany;
        private Integer pstock;
        private String imageUrl;

        public static Response from(Product product) {
            return Response.builder()
                    .pID(product.getPID())
                    .pname(product.getPname())
                    .pprice(product.getPprice())
                    .pcategory(product.getPcategory())
                    .description(product.getDescription())
                    .pcompany(product.getPcompany())
                    .pstock(product.getPstock())
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
