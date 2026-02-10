package com.lookfit.fitting.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI 가상 피팅 엔티티
 */
@Entity
@Table(name = "virtual_fitting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualFitting {

    @Id
    @Column(name = "fitting_id", length = 36)
    private String fittingId;

    @Column(name = "memberid", nullable = false, length = 50)
    private String memberId;

    @Column(name = "pID", nullable = false, length = 30)
    private String productId;

    @Column(name = "user_image_url", length = 500)
    private String userImageUrl;

    @Column(name = "result_image_url", length = 500)
    private String resultImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private FittingStatus status = FittingStatus.PENDING;

    @Column(name = "category", length = 20)
    private String category;

    @Column(name = "replicate_prediction_id", length = 100)
    private String replicatePredictionId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 피팅 완료 처리
     */
    public void complete(String resultImageUrl) {
        this.status = FittingStatus.COMPLETED;
        this.resultImageUrl = resultImageUrl;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 피팅 실패 처리
     */
    public void fail(String errorMessage) {
        this.status = FittingStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 피팅 처리 시작
     */
    public void startProcessing(String replicatePredictionId) {
        this.status = FittingStatus.PROCESSING;
        this.replicatePredictionId = replicatePredictionId;
    }
}
