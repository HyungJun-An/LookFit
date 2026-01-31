package com.lookfit.order.domain;

import com.lookfit.product.domain.BQna;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "c_qna")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CQnaId.class)
public class CQna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "c_no")
    private Integer cNo;

    @Id
    @Column(name = "c_ref", nullable = false)
    private Integer cRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_ref", referencedColumnName = "b_no", insertable = false, updatable = false)
    private BQna bQna;

    @Column(name = "c_writer", length = 15)
    private String cWriter;

    @Builder.Default
    @Column(name = "c_comment_date")
    private LocalDateTime cCommentDate = LocalDateTime.now();

    @Column(name = "c_content", columnDefinition = "TEXT")
    private String cContent;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
