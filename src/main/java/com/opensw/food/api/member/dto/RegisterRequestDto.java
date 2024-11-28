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
public class RegisterRequestDto {

    @NotBlank(message = "")
    private String email;

    @NotBlank(message = "")
    @Size(min = 2, message = "")
    private String nickname;

    @NotBlank(message = "")
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
