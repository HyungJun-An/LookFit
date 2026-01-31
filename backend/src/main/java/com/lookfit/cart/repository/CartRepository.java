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
    List<Cart> findByMemberid(String memberid);

    @Query("SELECT c FROM Cart c WHERE c.memberid = :memberid AND c.pID = :pID")
    Optional<Cart> findByMemberidAndProductId(@Param("memberid") String memberid, @Param("pID") String pID);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.memberid = :memberid AND c.pID = :pID")
    void deleteByMemberidAndProductId(@Param("memberid") String memberid, @Param("pID") String pID);
}
