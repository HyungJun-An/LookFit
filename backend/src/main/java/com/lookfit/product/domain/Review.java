package com.lookfit.product.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "product_id", length = 30, nullable = false)
    private String productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "pID", insertable = false, updatable = false)
    private Product product;

    @Column(name = "member_id", length = 100, nullable = false)
    private String memberId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "original_filename", length = 200)
    private String originalFilename;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 리뷰 수정
     */
    public void update(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 이미지 URL 수정
     */
    public void updateImage(String imageUrl, String originalFilename) {
        this.imageUrl = imageUrl;
        this.originalFilename = originalFilename;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Soft delete
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 작성자 확인
     */
    public boolean isOwner(String memberId) {
        return this.memberId.equals(memberId);
    }
}
