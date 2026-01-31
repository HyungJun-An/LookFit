package com.lookfit.product.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "b_qna")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BQna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "b_no")
    private Integer bNo;

    @Column(name = "b_title", length = 50)
    private String bTitle;

    @Column(name = "b_pwd")
    private Integer bPwd; // 비밀글비밀번호

    @Column(name = "b_writer", length = 15)
    private String bWriter;

    @Column(name = "b_content", columnDefinition = "TEXT")
    private String bContent;

    @Builder.Default
    @Column(name = "b_date")
    private LocalDateTime bDate = LocalDateTime.now();

    @Builder.Default
    @Column(name = "b_readcount")
    private Integer bReadcount = 0;

    @Column(name = "b_category", length = 50)
    private String bCategory;

    @Column(name = "pID", length = 30)
    private String pID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pID", referencedColumnName = "pID", insertable = false, updatable = false)
    private Product product;

    @Builder.Default
    @Column(name = "lock_flag", length = 1)
    private String lockFlag = "N";

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
