package com.lookfit.order.repository;

import com.lookfit.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 특정 주문의 상품 목록 조회
     */
    List<OrderItem> findByOrderno(Integer orderno);
}
