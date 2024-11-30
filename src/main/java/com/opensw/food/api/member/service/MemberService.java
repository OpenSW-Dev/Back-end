package com.opensw.food.api.member.service;


import com.opensw.food.api.member.dto.LoginRequestDto;
import com.opensw.food.api.member.dto.RegisterRequestDto;
import com.opensw.food.api.member.entity.Member;
import com.opensw.food.api.member.jwt.JwtProvider;
import com.opensw.food.api.member.repository.MemberRepository;
import com.opensw.food.common.exception.NotFoundException;
import com.opensw.food.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public Member registerMember(RegisterRequestDto requestDto) {

        if (memberRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("Nickname is already in use");
        }

        Member member = requestDto.toMember();
        member.encodePassword(passwordEncoder);

        return memberRepository.save(member);
    }

    public String loginMember(LoginRequestDto requestDto) {
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("Wrong Email or Wrong password");
        }

        return jwtProvider.generateToken(member.getEmail(), member.getRole().name());
    }

    public Long getUserIdByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
        return member.getMemberId();
    }
}
