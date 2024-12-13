package com.opensw.food.api.member.dto;

import com.opensw.food.api.member.entity.Member;
import lombok.Getter;

@Getter
public class UserInfoResponseDTO {

    private final Long id;
    private final String nickname;
    private final String email;
    private final int following;

    public UserInfoResponseDTO(Member member, int followerCount) {
        this.id = member.getMemberId();
        this.nickname = member.getNickname();
        this.following = followerCount;
        this.email = member.getEmail();
    }
}
