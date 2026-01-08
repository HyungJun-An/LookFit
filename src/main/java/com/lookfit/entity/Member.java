package com.lookfit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @Column(name = "memberid", length = 50) // 이메일 등을 포함하기 위해 넉넉히 잡았습니다.
    private String memberid;

    @Column(nullable = false)
    private String membername;

    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 1)
    private String gender; // M, F 등

    private Integer age;

    @Column(length = 1)
    private String regflag; // 'S' (Social), 'G' (General)

    @Column(length = 20)
    private String grade; // USER, ADMIN 등

    @Builder.Default
    private LocalDateTime enrolldate = LocalDateTime.now();

    @Builder.Default
    private String delflag = "N";
}