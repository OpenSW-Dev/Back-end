package com.opensw.food.api.comment.dto;

import com.opensw.food.api.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class CommentResponseDTO {

    private Long id;
    private String comment;
    private String nickname;
    private Long parentId;
    private String updatedAt;

    public static CommentResponseDTO fromEntity(Comment comment) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss");

        return CommentResponseDTO.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .nickname(comment.getMember().getNickname())
                .parentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .updatedAt(comment.getUpdatedAt().format(dateTimeFormatter))
                .build();
    }
}