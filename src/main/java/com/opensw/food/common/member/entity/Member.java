package com.opensw.food.common.member.entity;

import com.opensw.food.common.entity.BaseTimeEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private String token;

    public void addUserAuthority() {
        this.role = Role.USER;
    }

    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }
}
