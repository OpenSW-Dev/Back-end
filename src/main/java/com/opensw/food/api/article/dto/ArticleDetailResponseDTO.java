package com.opensw.food.api.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDetailResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String date;
    private String category;
    private long likeCnt;
    private List<String> images;
    private boolean myArticle;
    private boolean myLike;
    private Long authorId;
    private String nickname;
    //private String profileImage;
}