package com.opensw.food.api.member.dto;

import com.opensw.food.api.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RegisterRequestDto {

    private String email;

    private String nickname;

    private String password;

    private String checkPassword;

    private Role role;
}
