package com.lookfit.order.domain;

import com.lookfit.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "buy")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Buy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderno")
    private Integer orderno;

    @Column(name = "res_name", length = 20)
    private String resName; // 받는사람이름

    @Column(name = "res_address", length = 100)
    private String resAddress; // 받는사람주소

    @Column(name = "res_phone", length = 11)
    private String resPhone; // 받는사람전화번호

    @Column(name = "memberid", length = 15, nullable = false)
    private String memberid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberid", referencedColumnName = "memberid", insertable = false, updatable = false)
    private Member member;

    @Column(name = "res_requirement", length = 100)
    private String resRequirement; // 요청사항

    @Column(name = "totalprice", precision = 10, scale = 0)
    private BigDecimal totalprice;

    @Builder.Default
    @Column(name = "orderdate")
    private LocalDateTime orderdate = LocalDateTime.now();
}
