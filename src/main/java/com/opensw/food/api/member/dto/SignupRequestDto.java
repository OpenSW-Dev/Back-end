package com.opensw.food.api.member.dto;

import com.opensw.food.api.member.entity.Member;
import com.opensw.food.api.member.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SignupRequestDto {

    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(min = 2, message = "닉네임이 너무 짧습니다.")
    private String nickname;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    private String checkPassword;

    private Role role;

    @Builder
    public Member toMember() {
        return Member.builder()
                .email(email)
                .nickname(nickname)
                .password(password)
                .role(Role.USER)
                .build();
    }
}
