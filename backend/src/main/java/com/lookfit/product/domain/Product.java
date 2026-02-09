package com.lookfit.product.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @Column(name = "pID", length = 30)
    private String productId;

    @Column(name = "pname", length = 30)
    private String productName;

    @Column(name = "pprice", precision = 10, scale = 0)
    private BigDecimal productPrice;

    @Column(name = "pcategory", length = 30)
    private String productCategory;

    @Column(name = "description", length = 50)
    private String description;

    @Column(name = "pcompany", length = 30)
    private String productCompany;

    @Column(name = "pstock")
    private Integer productStock;

    @Column(name = "imageurl", length = 500)
    private String imageUrl;
}
