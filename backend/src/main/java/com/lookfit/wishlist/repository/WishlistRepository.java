package com.lookfit.wishlist.repository;

import com.lookfit.wishlist.domain.Wishlist;
import com.lookfit.wishlist.domain.WishlistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {

    /**
     * 특정 사용자의 찜 목록 조회
     */
    List<Wishlist> findByMemberId(String memberId);

    /**
     * 특정 사용자가 특정 상품을 찜했는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM Wishlist w WHERE w.memberId = :memberId AND w.productId = :productId")
    boolean existsByMemberIdAndProductId(@Param("memberId") String memberId, @Param("productId") String productId);

    /**
     * 특정 사용자의 찜 목록 삭제
     */
    @Query("DELETE FROM Wishlist w WHERE w.memberId = :memberId AND w.productId = :productId")
    @Modifying
    void deleteByMemberIdAndProductId(@Param("memberId") String memberId, @Param("productId") String productId);

    /**
     * 특정 사용자의 찜 개수 조회
     */
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.memberId = :memberId")
    long countByMemberId(@Param("memberId") String memberId);

    /**
     * 특정 상품의 찜 개수 조회 (인기도 측정용)
     */
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.productId = :productId")
    long countByProductId(@Param("productId") String productId);

    /**
     * 특정 사용자의 찜 목록 조회 (Product와 JOIN)
     */
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product WHERE w.memberId = :memberId ORDER BY w.addedAt DESC")
    List<Wishlist> findByMemberIdWithProduct(@Param("memberId") String memberId);
}
