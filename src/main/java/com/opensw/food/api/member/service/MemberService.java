package com.opensw.food.api.member.service;


import com.opensw.food.api.article.repository.ArticleRepository;
import com.opensw.food.api.comment.repository.CommentRepository;
import com.opensw.food.api.member.dto.FollowedUserDTO;
import com.opensw.food.api.member.dto.LoginRequestDto;
import com.opensw.food.api.member.dto.SignupRequestDto;
import com.opensw.food.api.member.entity.Follow;
import com.opensw.food.api.member.entity.Member;
import com.opensw.food.api.member.entity.Role;
import com.opensw.food.api.member.jwt.JwtProvider;
import com.opensw.food.api.member.repository.FollowRepository;
import com.opensw.food.api.member.repository.MemberRepository;
import com.opensw.food.common.exception.NotFoundException;
import com.opensw.food.common.exception.UnauthorizedException;
import com.opensw.food.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final FollowRepository followRepository;
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;

    public void signupMember(SignupRequestDto requestDto) {

        Member member = Member.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .nickname(requestDto.getNickname())
                .role(Role.USER)
                .build();

        memberRepository.save(member);
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

    public Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException(ErrorStatus.USER_UNAUTHORIZED.getMessage());
        }

        String email = authentication.getName();

        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }

    @Transactional
    public boolean followOrUnfollowMember(Long userId, Long followingId) {
        // 팔로우 하는 유저를 찾을 수 없을 경우 예외처리
        Member follower = memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        // 팔로우 할려는 유저를 찾을 수 없을 경우 예외처리
        Member following = memberRepository.findById(followingId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        // 팔로우 상태인지 확인
        return followRepository.findByFollowerAndFollowing(follower, following)
                .map(follow -> {
                    followRepository.delete(follow);
                    return false; // 팔로우 해지됨
                })
                .orElseGet(() -> {
                    Follow newFollow = Follow.builder()
                            .follower(follower)
                            .following(following)
                            .build();
                    followRepository.save(newFollow);
                    return true; // 팔로우 추가됨
                });
    }

    @Transactional(readOnly = true)
    public List<FollowedUserDTO> getFollowedUsers(Long userId) {
        // 해당 유저를 찾을 수 없을 경우 예외처리
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        return member.getFollowing().stream()
                .map(follow -> new FollowedUserDTO(
                        follow.getFollowing().getMemberId(),
                        follow.getFollowing().getNickname()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        commentRepository.deleteAllByMember(member);
        articleRepository.deleteAllByMember(member);
        followRepository.deleteByFollowerOrFollowing(member, member);
        memberRepository.delete(member);
    }
}
