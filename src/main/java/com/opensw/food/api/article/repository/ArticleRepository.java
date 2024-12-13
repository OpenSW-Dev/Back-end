package com.opensw.food.api.article.repository;

import com.opensw.food.api.article.entity.Article;
import com.opensw.food.api.member.entity.Member;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findArticlesByMemberMemberId(Long memberId);

    void deleteAllByMember(Member member);
}
