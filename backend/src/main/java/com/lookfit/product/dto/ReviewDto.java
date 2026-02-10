package com.lookfit.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lookfit.product.domain.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewDto {

    /**
     * 리뷰 작성 요청
     */
    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "별점은 필수입니다")
        @Min(value = 1, message = "별점은 1~5 사이여야 합니다")
        @Max(value = 5, message = "별점은 1~5 사이여야 합니다")
        @JsonProperty("rating")
        private Integer rating;

        @NotBlank(message = "리뷰 내용은 필수입니다")
        @JsonProperty("content")
        private String content;
    }

    /**
     * 리뷰 수정 요청
     */
    @Getter
    @Setter
    public static class UpdateRequest {

        @Min(value = 1, message = "별점은 1~5 사이여야 합니다")
        @Max(value = 5, message = "별점은 1~5 사이여야 합니다")
        @JsonProperty("rating")
        private Integer rating;

        @JsonProperty("content")
        private String content;
    }

    /**
     * 리뷰 응답
     */
    @Getter
    @Builder
    public static class Response {

        @JsonProperty("reviewId")
        private Long reviewId;

        @JsonProperty("productId")
        private String productId;

        @JsonProperty("memberId")
        private String memberId;

        @JsonProperty("rating")
        private Integer rating;

        @JsonProperty("content")
        private String content;

        @JsonProperty("imageUrl")
        private String imageUrl;

        @JsonProperty("createdAt")
        private LocalDateTime createdAt;

        @JsonProperty("updatedAt")
        private LocalDateTime updatedAt;

        @JsonProperty("isOwner")
        private boolean isOwner;

        public static Response from(Review review, String currentMemberId) {
            return Response.builder()
                    .reviewId(review.getReviewId())
                    .productId(review.getProductId())
                    .memberId(maskMemberId(review.getMemberId()))
                    .rating(review.getRating())
                    .content(review.getContent())
                    .imageUrl(review.getImageUrl())
                    .createdAt(review.getCreatedAt())
                    .updatedAt(review.getUpdatedAt())
                    .isOwner(review.getMemberId().equals(currentMemberId != null ? currentMemberId : ""))
                    .build();
        }

        /**
         * 회원 ID 마스킹 (개인정보 보호)
         * 예: "user@gmail.com" -> "us***@gmail.com"
         */
        private static String maskMemberId(String memberId) {
            if (memberId == null || memberId.length() <= 3) {
                return "***";
            }
            if (memberId.contains("@")) {
                String[] parts = memberId.split("@");
                String localPart = parts[0];
                if (localPart.length() <= 2) {
                    return localPart + "***@" + parts[1];
                }
                return localPart.substring(0, 2) + "***@" + parts[1];
            }
            return memberId.substring(0, 2) + "***";
        }
    }

    /**
     * 리뷰 요약 (평균 별점 + 리뷰 수)
     */
    @Getter
    @Builder
    public static class ReviewSummary {

        @JsonProperty("averageRating")
        private Double averageRating;

        @JsonProperty("reviewCount")
        private Long reviewCount;

        @JsonProperty("ratingDistribution")
        private int[] ratingDistribution; // [1점 수, 2점 수, 3점 수, 4점 수, 5점 수]
    }

    /**
     * 리뷰 페이징 응답
     */
    @Getter
    @Builder
    public static class ReviewPage {

        @JsonProperty("reviews")
        private List<Response> reviews;

        @JsonProperty("totalElements")
        private long totalElements;

        @JsonProperty("totalPages")
        private int totalPages;

        @JsonProperty("currentPage")
        private int currentPage;

        @JsonProperty("hasNext")
        private boolean hasNext;

        @JsonProperty("hasPrevious")
        private boolean hasPrevious;
    }
}
