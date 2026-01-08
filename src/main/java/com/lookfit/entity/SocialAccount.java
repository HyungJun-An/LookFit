package com.lookfit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "social_account")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer socialId;

    // 실무에서는 외래키(FK) 매핑을 권장합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberid", referencedColumnName = "memberid")
    private Member member;

    @Column(nullable = false, length = 20)
    private String provider; // google

    @Column(nullable = false, length = 100)
    private String providerUserId; // 구글/카카오에서 주는 고유 번호

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}