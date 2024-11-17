package com.opensw.food.common.member.dto;

import com.opensw.food.common.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MemberSignUpRequestDto {

    private String email;

    private String nickname;

    private String password;

    private String checkPassword;

    private Role role;
}
