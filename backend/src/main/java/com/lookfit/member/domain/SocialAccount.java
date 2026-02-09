package com.lookfit.member.domain;

import com.lookfit.member.domain.Member;
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
    @Column(name = "social_id")
    private Long socialId;

    @Column(name = "memberid", length = 50, nullable = false)
    private String memberid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberid", referencedColumnName = "memberid", insertable = false, updatable = false)
    private Member member;

    @Column(nullable = false, length = 20)
    private String provider; // google, kakao 등

    @Column(name = "provider_user_id", nullable = false, length = 100, unique = true)
    private String providerUserId; // 제공사에서 주는 고유 번호

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}