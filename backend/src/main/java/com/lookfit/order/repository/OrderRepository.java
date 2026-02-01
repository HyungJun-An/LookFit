package com.lookfit.order.repository;

import com.lookfit.order.domain.Buy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Buy, Integer> {

    /**
     * 특정 회원의 주문 목록을 페이징하여 조회
     * 최신순 정렬은 Pageable에서 처리
     */
    Page<Buy> findByMemberid(String memberid, Pageable pageable);

    /**
     * 주문번호와 회원ID로 주문 조회 (본인 주문 검증용)
     */
    Optional<Buy> findByOrdernoAndMemberid(Integer orderno, String memberid);
}
