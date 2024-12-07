package com.opensw.food.api.member.controller;

import com.opensw.food.api.member.dto.LoginRequestDto;
import com.opensw.food.api.member.dto.SignupRequestDto;
import com.opensw.food.api.member.entity.Member;
import com.opensw.food.api.member.service.MemberService;
import com.opensw.food.common.response.ApiResponse;
import com.opensw.food.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "Member 관련 API 입니다.")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(
            summary = "회원가입 API",
            description = "사용자의 정보를 등록합니다."
    )
    @PostMapping("/auth/signup")
    public ResponseEntity<ApiResponse<Member>> signup(@Validated @RequestBody SignupRequestDto signupRequestDto) {
        Member newMember = memberService.signupMember(signupRequestDto);
        return ApiResponse.success(SuccessStatus.SEND_SIGNUP_SUCCESS, newMember);
    }

    @Operation(
            summary = "로그인 API",
            description = "사용자의 정보를 확인 및 토큰을 발급합니다."
    )
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<String>> login(@Validated @RequestBody LoginRequestDto loginRequestDto) {
        String token = memberService.loginMember(loginRequestDto);
        return ApiResponse.success(SuccessStatus.SEND_LOGIN_SUCCESS, token);
    }

    @GetMapping("/members/{email}")
    public ResponseEntity<ApiResponse<Long>> getMemberByEmail(@PathVariable String email) {
        Long memberId = memberService.getUserIdByEmail(email);
        return ApiResponse.success(SuccessStatus.GET_MEMBER_SUCCESS, memberId);
    }

    @GetMapping("/members/me")
    public ResponseEntity<ApiResponse<Member>> getCurrentMember() {
        Member currentMember = memberService.getCurrentMember();
        return ApiResponse.success(SuccessStatus.GET_CURRENT_MEMBER_SUCCESS, currentMember);
    }
}
