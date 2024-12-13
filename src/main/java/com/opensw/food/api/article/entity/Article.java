package com.opensw.food.api.article.entity;

import com.opensw.food.api.member.entity.Member;
import com.opensw.food.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "article")
@Builder(toBuilder = true)
public class Article extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long id;

    private String category;
    private String title;
    private long likeCnt;
    private long cmtCnt;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ArticleImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ArticleLike> articleLikes = new ArrayList<>();

    // 게시글 이미지 추가
    public void addImages(List<String> imageUrls) {
        for (String url : imageUrls) {
            ArticleImage image = ArticleImage.builder()
                    .imageUrl(url)
                    .article(this)
                    .build();
            this.images.add(image);
        }
    }

    // 좋아요 증가
    public Article increaseLikeCnt() {
        return this.toBuilder()
                .likeCnt(this.likeCnt + 1)
                .build();
    }

    // 좋아요 감소
    public Article decreaseLikeCnt() {
        return this.toBuilder()
                .likeCnt(this.likeCnt - 1)
                .build();
    }

    // 댓글 수 증가
    public Article increaseCmtCnt() {
        return this.toBuilder()
                .cmtCnt(this.cmtCnt + 1)
                .build();
    }

    // 댓굴 수 감소
    public Article decreaseCmtCnt() {
        return this.toBuilder()
                .cmtCnt(this.cmtCnt - 1)
                .build();
    }
}
