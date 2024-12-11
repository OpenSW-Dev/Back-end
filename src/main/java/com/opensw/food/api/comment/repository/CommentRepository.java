package com.opensw.food.api.comment.repository;

import com.opensw.food.api.article.entity.Article;
import com.opensw.food.api.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticle(Article article);
    List<Comment> findByParentComment(Comment parentComment);
}