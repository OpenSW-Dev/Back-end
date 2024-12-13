package com.opensw.food.api.member.repository;

import com.opensw.food.api.member.entity.Follow;
import com.opensw.food.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerAndFollowing(Member follower, Member following);

    void deleteByFollowerOrFollowing(Member follower, Member following);
}