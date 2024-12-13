package com.opensw.food.api.member.controller;

import com.opensw.food.api.member.dto.*;
import com.opensw.food.api.member.entity.Member;
import com.opensw.food.api.member.service.MemberService;
import com.opensw.food.common.response.ApiResponse;
import com.opensw.food.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse<Void>> signup(@Validated @RequestBody SignupRequestDto signupRequestDto) {
        memberService.signupMember(signupRequestDto);
        return ApiResponse.success_only(SuccessStatus.SEND_SIGNUP_SUCCESS);
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

    @Operation(
            summary = "사용자 정보 조회 API",
            description = "사용자 정보를 조회합니다."
    )
    @GetMapping("/members/me")
    public ResponseEntity<ApiResponse<UserInfoResponseDTO>> getCurrentMember(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = memberService.getUserIdByEmail(userDetails.getUsername());
        UserInfoResponseDTO userInfo = memberService.getCurrentMember(userId);
        return ApiResponse.success(SuccessStatus.GET_CURRENT_MEMBER_SUCCESS, userInfo);
    }

    @Operation(summary = "사용자 팔로우, 언팔로우 API", description = "특정 사용자를 팔로우하거나, 이미 팔로우한 경우 해지합니다.")
    @PostMapping("/follow")
    public ResponseEntity<ApiResponse<Void>> followMember(@AuthenticationPrincipal UserDetails userDetails,
                                                          @RequestBody FollowRequestDTO followRequestDTO) {
        Long userId = memberService.getUserIdByEmail(userDetails.getUsername());
        boolean isFollowed = memberService.followOrUnfollowMember(userId, followRequestDTO.getFollowingId());

        if(isFollowed){
            return ApiResponse.success_only(SuccessStatus.USER_FOLLOW_SUCCESS);
        }else{
            return ApiResponse.success_only(SuccessStatus.USER_UNFOLLOW_SUCCESS);
        }
    }

    @Operation(
            summary = "팔로우 중인 사용자 목록 조회 API",
            description = "현재 사용자가 팔로우하고 있는 사용자 목록을 반환합니다."
    )
    @GetMapping("/follow")
    public ResponseEntity<ApiResponse<List<FollowedUserDTO>>> getFollowedUsers(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = memberService.getUserIdByEmail(userDetails.getUsername());
        List<FollowedUserDTO> followedUsers = memberService.getFollowedUsers(userId);
        return ApiResponse.success(SuccessStatus.GET_FOLLOWED_USERS_SUCCESS, followedUsers);
    }

    @Operation(
            summary = "회원 탈퇴 API",
            description = "현재 사용자를 회원 탈퇴합니다."
    )
    @PostMapping("/auth/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdrawMember(Member member) {
        Long memberId = memberService.getUserIdByEmail(member.getEmail());
        memberService.deleteMember(memberId);

        return ApiResponse.success_only(SuccessStatus.MEMBER_WITHDRAW_SUCCESS);
    }
}
