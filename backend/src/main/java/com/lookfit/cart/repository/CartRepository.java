package com.lookfit.cart.repository;

import com.lookfit.cart.domain.Cart;
import com.lookfit.cart.domain.CartId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, CartId> {
    List<Cart> findByMemberId(String memberId);

    @Query("SELECT c FROM Cart c WHERE c.memberId = :memberId AND c.productId = :productId")
    Optional<Cart> findByMemberIdAndProductId(@Param("memberId") String memberId, @Param("productId") String productId);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.memberId = :memberId AND c.productId = :productId")
    void deleteByMemberIdAndProductId(@Param("memberId") String memberId, @Param("productId") String productId);
}
