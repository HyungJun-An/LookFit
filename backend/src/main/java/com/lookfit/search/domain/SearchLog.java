package com.lookfit.search.domain;

import com.lookfit.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_id")
    private Integer searchId;

    @Column(name = "keyword", length = 100, nullable = false)
    private String keyword;

    @Builder.Default
    @Column(name = "searched_at")
    private LocalDateTime searchedAt = LocalDateTime.now();

    @Column(name = "memberid", length = 15)
    private String memberid; // 비회원은 NULL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberid", referencedColumnName = "memberid", insertable = false, updatable = false)
    private Member member;
}
