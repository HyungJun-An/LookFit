package com.lookfit.order.domain;

import com.lookfit.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "orderno", nullable = false)
    private Integer orderno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderno", referencedColumnName = "orderno", insertable = false, updatable = false)
    private Buy buy;

    @Column(name = "pID", length = 30, nullable = false)
    private String productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pID", referencedColumnName = "pID", insertable = false, updatable = false)
    private Product product;

    @Column(name = "pname", length = 30)
    private String productName;

    @Column(name = "pprice", precision = 10, scale = 0)
    private BigDecimal productPrice;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "subtotal", precision = 10, scale = 0)
    private BigDecimal subtotal;
}
