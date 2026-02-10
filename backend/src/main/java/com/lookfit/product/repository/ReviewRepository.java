package com.lookfit.product.repository;

import com.lookfit.product.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 상품별 리뷰 목록 조회 (삭제되지 않은 것만, 페이징)
     */
    Page<Review> findByProductIdAndDeletedAtIsNull(String productId, Pageable pageable);

    /**
     * 상품별 평균 별점 조회
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.productId = :productId AND r.deletedAt IS NULL")
    Double getAverageRatingByProductId(@Param("productId") String productId);

    /**
     * 상품별 리뷰 수 조회
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.deletedAt IS NULL")
    Long countByProductIdAndNotDeleted(@Param("productId") String productId);

    /**
     * 특정 사용자가 특정 상품에 대해 이미 리뷰를 작성했는지 확인
     */
    boolean existsByProductIdAndMemberIdAndDeletedAtIsNull(String productId, String memberId);
}
