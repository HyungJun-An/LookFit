package com.lookfit.fitting.repository;

import com.lookfit.fitting.domain.FittingStatus;
import com.lookfit.fitting.domain.VirtualFitting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 가상 피팅 Repository
 */
@Repository
public interface VirtualFittingRepository extends JpaRepository<VirtualFitting, String> {

    /**
     * 회원의 피팅 기록 조회 (최신순)
     */
    Page<VirtualFitting> findByMemberIdOrderByCreatedAtDesc(String memberId, Pageable pageable);

    /**
     * 회원의 특정 상태 피팅 조회
     */
    List<VirtualFitting> findByMemberIdAndStatus(String memberId, FittingStatus status);

    /**
     * 회원의 특정 상품 피팅 기록 조회
     */
    List<VirtualFitting> findByMemberIdAndProductId(String memberId, String productId);

    /**
     * Replicate Prediction ID로 조회
     */
    Optional<VirtualFitting> findByReplicatePredictionId(String replicatePredictionId);

    /**
     * 처리 중인 피팅 개수 조회 (부하 체크용)
     */
    @Query("SELECT COUNT(vf) FROM VirtualFitting vf WHERE vf.status = :status")
    long countByStatus(@Param("status") FittingStatus status);

    /**
     * 회원의 피팅 개수 조회
     */
    long countByMemberId(String memberId);

    /**
     * 특정 기간 동안 생성된 피팅 조회
     */
    @Query("SELECT vf FROM VirtualFitting vf WHERE vf.createdAt >= :startDate ORDER BY vf.createdAt DESC")
    List<VirtualFitting> findRecentFittings(@Param("startDate") java.time.LocalDateTime startDate);
}
