package com.lookfit.fitting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lookfit.fitting.domain.FittingStatus;
import com.lookfit.fitting.domain.VirtualFitting;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 가상 피팅 DTO
 */
public class FittingDto {

    /**
     * 사용자 이미지 업로드 요청
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadRequest {
        @JsonProperty("productId")
        private String productId;

        @JsonProperty("category")
        private String category;  // upper_body, lower_body, dresses
    }

    /**
     * 업로드 응답
     */
    @Getter
    @Builder
    public static class UploadResponse {
        @JsonProperty("fittingId")
        private String fittingId;

        @JsonProperty("userImageUrl")
        private String userImageUrl;

        @JsonProperty("status")
        private String status;

        @JsonProperty("message")
        private String message;

        public static UploadResponse from(VirtualFitting fitting) {
            return UploadResponse.builder()
                    .fittingId(fitting.getFittingId())
                    .userImageUrl(fitting.getUserImageUrl())
                    .status(fitting.getStatus().name())
                    .message("사용자 이미지 업로드 완료. AI 생성 요청 대기 중")
                    .build();
        }
    }

    /**
     * AI 생성 요청
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateRequest {
        @JsonProperty("fittingId")
        private String fittingId;
    }

    /**
     * AI 생성 응답
     */
    @Getter
    @Builder
    public static class GenerateResponse {
        @JsonProperty("fittingId")
        private String fittingId;

        @JsonProperty("status")
        private String status;

        @JsonProperty("replicatePredictionId")
        private String replicatePredictionId;

        @JsonProperty("estimatedTime")
        private String estimatedTime;

        @JsonProperty("message")
        private String message;

        public static GenerateResponse from(VirtualFitting fitting) {
            return GenerateResponse.builder()
                    .fittingId(fitting.getFittingId())
                    .status(fitting.getStatus().name())
                    .replicatePredictionId(fitting.getReplicatePredictionId())
                    .estimatedTime("10-30초")
                    .message("AI 이미지 생성 중입니다. 잠시 후 결과를 확인해주세요.")
                    .build();
        }
    }

    /**
     * 피팅 상세 응답
     */
    @Getter
    @Builder
    public static class DetailResponse {
        @JsonProperty("fittingId")
        private String fittingId;

        @JsonProperty("memberId")
        private String memberId;

        @JsonProperty("productId")
        private String productId;

        @JsonProperty("userImageUrl")
        private String userImageUrl;

        @JsonProperty("resultImageUrl")
        private String resultImageUrl;

        @JsonProperty("status")
        private String status;

        @JsonProperty("statusDisplay")
        private String statusDisplay;

        @JsonProperty("category")
        private String category;

        @JsonProperty("errorMessage")
        private String errorMessage;

        @JsonProperty("createdAt")
        private LocalDateTime createdAt;

        @JsonProperty("completedAt")
        private LocalDateTime completedAt;

        public static DetailResponse from(VirtualFitting fitting) {
            return DetailResponse.builder()
                    .fittingId(fitting.getFittingId())
                    .memberId(fitting.getMemberId())
                    .productId(fitting.getProductId())
                    .userImageUrl(fitting.getUserImageUrl())
                    .resultImageUrl(fitting.getResultImageUrl())
                    .status(fitting.getStatus().name())
                    .statusDisplay(fitting.getStatus().getDisplayName())
                    .category(fitting.getCategory())
                    .errorMessage(fitting.getErrorMessage())
                    .createdAt(fitting.getCreatedAt())
                    .completedAt(fitting.getCompletedAt())
                    .build();
        }
    }

    /**
     * 피팅 기록 목록 응답
     */
    @Getter
    @Builder
    public static class HistoryResponse {
        @JsonProperty("fittings")
        private List<DetailResponse> fittings;

        @JsonProperty("totalCount")
        private long totalCount;

        @JsonProperty("page")
        private int page;

        @JsonProperty("pageSize")
        private int pageSize;

        @JsonProperty("totalPages")
        private int totalPages;
    }

    /**
     * 피팅 상태 조회 응답 (폴링용)
     */
    @Getter
    @Builder
    public static class StatusResponse {
        @JsonProperty("fittingId")
        private String fittingId;

        @JsonProperty("status")
        private String status;

        @JsonProperty("resultImageUrl")
        private String resultImageUrl;

        @JsonProperty("errorMessage")
        private String errorMessage;

        @JsonProperty("isCompleted")
        private boolean isCompleted;

        public static StatusResponse from(VirtualFitting fitting) {
            boolean completed = fitting.getStatus() == FittingStatus.COMPLETED ||
                               fitting.getStatus() == FittingStatus.FAILED;

            return StatusResponse.builder()
                    .fittingId(fitting.getFittingId())
                    .status(fitting.getStatus().name())
                    .resultImageUrl(fitting.getResultImageUrl())
                    .errorMessage(fitting.getErrorMessage())
                    .isCompleted(completed)
                    .build();
        }
    }
}
