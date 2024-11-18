package com.opensw.food.api.member.entity;

import com.opensw.food.common.entity.BaseTimeEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private String token;

    private Instant expiredTime;

    public void addUserAuthority() {
        this.role = Role.USER;
    }

    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }

    public boolean isAccountNonExpired() {
        return expiredTime == null || expiredTime.isAfter(Instant.now());
    }
}
