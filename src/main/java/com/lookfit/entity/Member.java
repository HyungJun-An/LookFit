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
    @Column(name = "memberid", length = 50)
    private String memberid;

    @Column(nullable = false)
    private String membername;

    @Column(length = 1)
    private String gender; // M, F

    private Integer age;

    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String favorite; // 선호도

    @Column(length = 20)
    private String grade; // USER, ADMIN

    @Builder.Default
    private LocalDateTime enrolldate = LocalDateTime.now();

    private String delflag;

    private LocalDateTime deletedate;

    @Column(length = 1)
    private String regflag; // 'S' (Social), 'G' (General)

    @Column(length = 300)
    private String password; // 비밀번호
}