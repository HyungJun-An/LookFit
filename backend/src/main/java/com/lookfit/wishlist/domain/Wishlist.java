package com.lookfit.wishlist.domain;

import com.lookfit.member.domain.Member;
import com.lookfit.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 찜하기 엔티티
 * 사용자가 좋아하는 상품을 저장
 */
@Entity
@Table(name = "wishlist")
@IdClass(WishlistId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {

    @Id
    @Column(name = "memberid", length = 100)
    private String memberId;

    @Id
    @Column(name = "pID", length = 30)
    private String productId;

    @Builder.Default
    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberid", referencedColumnName = "memberid", insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pID", referencedColumnName = "pID", insertable = false, updatable = false)
    private Product product;
}
