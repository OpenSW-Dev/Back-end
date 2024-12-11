package com.opensw.food.api.article.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyArticleListResponseDTO {

    private Long id;
    private String title;
    private String content;
    private String category;
    private long likeCnt;
    private List<String> images;
}