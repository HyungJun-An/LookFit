package com.lookfit.cart.domain;

import com.lookfit.member.domain.Member;
import com.lookfit.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cart")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CartId.class)
public class Cart {

    @Id
    @Column(name = "pID", length = 30, nullable = false)
    private String pID;

    @Id
    @Column(name = "memberid", length = 100, nullable = false)
    private String memberid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pID", referencedColumnName = "pID", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberid", referencedColumnName = "memberid", insertable = false, updatable = false)
    private Member member;

    @Column(name = "pname", length = 30)
    private String pname;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "pprice", precision = 10, scale = 0)
    private BigDecimal pprice;

    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
