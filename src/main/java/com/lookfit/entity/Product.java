package com.lookfit.entity;

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
    private String pID;

    @Column(name = "pname", length = 30)
    private String pname;

    @Column(name = "pprice", precision = 10, scale = 0)
    private BigDecimal pprice;

    @Column(name = "pcategory", length = 30)
    private String pcategory;

    @Column(name = "description", length = 50)
    private String description;

    @Column(name = "pcompany", length = 30)
    private String pcompany;

    @Column(name = "pstock")
    private Integer pstock;
}
