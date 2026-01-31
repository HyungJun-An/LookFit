package com.lookfit.product.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "b_review")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BReviewId.class)
public class BReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "b_no")
    private Integer bNo;

    @Id
    @Column(name = "pID", length = 30, nullable = false)
    private String pID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pID", referencedColumnName = "pID", insertable = false, updatable = false)
    private Product product;

    @Column(name = "b_writer", length = 15)
    private String bWriter;

    @Column(name = "b_content", columnDefinition = "TEXT")
    private String bContent;

    @Builder.Default
    @Column(name = "rating")
    private Integer rating = 5; // 별점 (1~5)

    @Column(name = "b_original_filename", length = 100)
    private String bOriginalFilename;

    @Column(name = "b_renamed_filename", length = 100)
    private String bRenamedFilename;

    @Builder.Default
    @Column(name = "b_date")
    private LocalDateTime bDate = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
