package com.opensw.food.api.article.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleTotalListResponseDTO {
    private Long id;
    private String title;
    private String category;
}