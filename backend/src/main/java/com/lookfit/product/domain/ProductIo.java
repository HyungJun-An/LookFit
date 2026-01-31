package com.lookfit.product.domain;

import com.lookfit.member.domain.Member;
import com.lookfit.order.domain.Buy;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_io")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ProductIoId.class)
public class ProductIo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iono")
    private Integer iono;

    @Id
    @Column(name = "pID", length = 30, nullable = false)
    private String pID;

    @Id
    @Column(name = "orderno", nullable = false)
    private Integer orderno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pID", referencedColumnName = "pID", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderno", referencedColumnName = "orderno", insertable = false, updatable = false)
    private Buy buy;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "status", length = 10)
    private String status; // I: 입고, O: 출고

    @Column(name = "pDate")
    private LocalDateTime pDate;

    @Column(name = "memberid", length = 15, nullable = false)
    private String memberid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberid", referencedColumnName = "memberid", insertable = false, updatable = false)
    private Member member;
}
