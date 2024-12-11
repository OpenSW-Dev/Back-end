package com.opensw.food.api.comment.service;

import com.opensw.food.api.article.entity.Article;
import com.opensw.food.api.article.repository.ArticleRepository;
import com.opensw.food.api.comment.dto.CommentCreateDTO;
import com.opensw.food.api.comment.dto.CommentResponseDTO;
import com.opensw.food.api.comment.dto.CommentUpdateDTO;
import com.opensw.food.api.comment.entity.Comment;
import com.opensw.food.api.comment.repository.CommentRepository;
import com.opensw.food.api.member.entity.Member;
import com.opensw.food.api.member.repository.MemberRepository;
import com.opensw.food.common.exception.NotFoundException;
import com.opensw.food.common.exception.UnauthorizedException;
import com.opensw.food.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createComment(CommentCreateDTO commentCreateDTO, Long userId) {
        // 해당 유저를 찾을 수 없을 경우 예외처리
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
        // 해당 게시글을 찾을 수 없을 경우 예외처리
        Article article = articleRepository.findById(commentCreateDTO.getArticleId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.RESOURCE_NOT_FOUND.getMessage()));
        // 부모댓글 처리
        Comment parentComment = null;
        if (commentCreateDTO.getParentId() != null) {
            parentComment = commentRepository.findById(commentCreateDTO.getParentId())
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.PARENT_COMMENT_NOT_FOUND_EXCEPTION.getMessage()));
        }

        Comment comment = Comment.builder()
                .comment(commentCreateDTO.getComment())
                .article(article)
                .member(member)
                .parentComment(parentComment)
                .build();

        commentRepository.save(comment);

        // 댓글 수 증가
        // updatedArticle = article.increaseCommentCount();
        //articleRepository.save(updatedArticle);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByArticleId(Long articleId) {
        // 해당 게시글을 찾을 수 없을 경우 예외처리
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.RESOURCE_NOT_FOUND.getMessage()));

        List<Comment> comments = commentRepository.findByArticle(article);

        return comments.stream()
                .map(CommentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateComment(Long commentId, CommentUpdateDTO commentUpdateDTO, Long userId) {
        // 댓글이 존재하지 않으면 예외 처리
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.COMMENT_NOT_FOUND_EXCEPTION.getMessage()));

        // 해당 댓글의 작성자가 요청한 사용자와 동일한지 검증
        if (!comment.getMember().getMemberId().equals(userId)) {
            throw new UnauthorizedException(ErrorStatus.COMMENT_WRITER_NOT_SAME_USER_EXCEPTION.getMessage());
        }

        comment = comment.toBuilder()
                .comment(commentUpdateDTO.getComment())
                .build();

        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        // 댓글을 ID로 찾고, 존재하지 않으면 예외 처리
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.COMMENT_NOT_FOUND_EXCEPTION.getMessage()));

        // 해당 댓글의 작성자가 요청한 사용자와 동일한지 검증
        if (!comment.getMember().getMemberId().equals(userId)) {
            throw new UnauthorizedException(ErrorStatus.COMMENT_WRITER_NOT_SAME_USER_EXCEPTION.getMessage());
        }

        // 먼저 자식 댓글들 삭제
        deleteChildComments(comment);

        commentRepository.delete(comment);
    }

    private void deleteChildComments(Comment parentComment) {
        List<Comment> childComments = commentRepository.findByParentComment(parentComment);
        for (Comment child : childComments) {
            deleteChildComments(child);
            commentRepository.delete(child);
        }
    }
}
