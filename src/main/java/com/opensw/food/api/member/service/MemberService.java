package com.opensw.food.api.member.service;

import com.opensw.food.api.member.dto.LoginRequestDto;
import com.opensw.food.api.member.dto.RegisterRequestDto;
import com.opensw.food.api.member.entity.Member;

import java.util.Optional;

public interface MemberService {

    String login(LoginRequestDto loginRequestDto) throws Exception;

    Long register(RegisterRequestDto registerRequestDto) throws Exception;

    String generateToken(Member member);

    Optional<Member> findByToken(String token);
}
