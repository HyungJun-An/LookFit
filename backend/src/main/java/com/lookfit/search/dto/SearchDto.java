package com.lookfit.search.dto;

import com.lookfit.search.domain.ProductDocument;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class SearchDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        private String keyword;
        private String category;
        private Double minPrice;
        private Double maxPrice;

        @Builder.Default
        private String sortBy = "relevance";  // relevance, price_asc, price_desc

        @Builder.Default
        private int page = 0;

        @Builder.Default
        private int size = 20;

        @Builder.Default
        private boolean inStockOnly = false;  // 재고 있는 상품만
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResponse {
        private String pid;
        private String pname;
        private BigDecimal pprice;
        private String pcategory;
        private String imageUrl;
        private Double relevanceScore;

        /**
         * ProductDocument를 SearchResponse로 변환
         */
        public static SearchResponse from(ProductDocument document, Float score) {
            if (document == null) {
                return null;
            }

            return SearchResponse.builder()
                    .pid(document.getPID())
                    .pname(document.getPname())
                    .pprice(document.getPprice())
                    .pcategory(document.getPcategory())
                    .imageUrl(document.getImageUrl())
                    .relevanceScore(score != null ? score.doubleValue() : 0.0)
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResultPage {
        private List<SearchResponse> content;
        private long totalElements;
        private int totalPages;
        private String keyword;
        private long searchTime;  // 검색 소요 시간 (ms)
        private int currentPage;
        private int pageSize;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularSearch {
        private String keyword;
        private long searchCount;
        private int rank;  // 순위
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchSuggestion {
        private List<String> recentSearches;
        private List<PopularSearch> popularSearches;
    }
}
