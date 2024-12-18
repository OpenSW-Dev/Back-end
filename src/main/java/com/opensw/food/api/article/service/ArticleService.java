package com.opensw.food.api.article.service;

import com.opensw.food.api.article.dto.ArticleCreateRequestDTO;
import com.opensw.food.api.article.dto.ArticleDetailResponseDTO;
import com.opensw.food.api.article.dto.ArticleTotalListResponseDTO;
import com.opensw.food.api.article.dto.MyArticleListResponseDTO;
import com.opensw.food.api.article.entity.Article;
import com.opensw.food.api.article.entity.ArticleImage;
import com.opensw.food.api.article.entity.ArticleLike;
import com.opensw.food.api.article.repository.ArticleLikeRepository;
import com.opensw.food.api.article.repository.ArticleRepository;
import com.opensw.food.api.aws.s3.S3Service;
import com.opensw.food.api.comment.entity.Comment;
import com.opensw.food.api.comment.repository.CommentRepository; // 댓글 리포지토리 주입
import com.opensw.food.api.member.entity.Follow;
import com.opensw.food.api.member.entity.Member;
import com.opensw.food.api.member.repository.MemberRepository;
import com.opensw.food.api.member.service.MemberService;
import com.opensw.food.common.exception.BadRequestException;
import com.opensw.food.common.exception.NotFoundException;
import com.opensw.food.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final S3Service s3Service;
    private final MemberService memberService;
    private final CommentRepository commentRepository;

    private static final String BASE64_IMAGE_REGEX = "data:image/(png|jpeg|jpg|webp|bmp);base64,([A-Za-z0-9+/=]+)";

    // 게시글 생성
    public void createArticle(Long userId, ArticleCreateRequestDTO articleRequest, List<MultipartFile> images) throws IOException {

        // 유저 검증
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        // 게시글 생성
        Article article = Article.builder()
                .member(member)
                .title(articleRequest.getTitle())
                .content(articleRequest.getContent())
                .category(articleRequest.getCategory())
                .likeCnt(0)
                .cmtCnt(0)
                .build();

        // Base64 이미지 처리 및 추가
        List<String> base64ImageUrls = processBase64Images(articleRequest.getContent(), String.valueOf(userId));
        article.addImages(base64ImageUrls);

        // 추가 이미지 처리
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = s3Service.uploadArticleImages(String.valueOf(userId), images);
            article.addImages(imageUrls);
        }

        articleRepository.save(article);
    }

    private List<String> processBase64Images(String content, String userId) throws IOException {
        List<String> imageUrls = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return imageUrls;
        }

        Pattern pattern = Pattern.compile(BASE64_IMAGE_REGEX);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String mimeType = matcher.group(1);
            String base64Image = matcher.group(2);

            // 이미지 디코딩 및 S3 업로드
            byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
            String fileName = "inline-image-" + System.currentTimeMillis() + "." + mimeType;
            String s3Url = s3Service.uploadInlineImage(userId, fileName, decodedBytes);

            imageUrls.add(s3Url);
        }

        return imageUrls;
    }

    // 전체 게시글 조회
    public List<ArticleTotalListResponseDTO> getTotalArticle() {
        List<Article> articles = articleRepository.findAll();

        List<Article> sortedArticles = articles.stream()
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .toList();

        return sortedArticles.stream()
                .map(article -> {
                    String firstImageUrl = article.getImages().isEmpty()
                            ? null
                            : article.getImages().get(0).getImageUrl();

                    return new ArticleTotalListResponseDTO(
                            article.getId(),
                            article.getTitle(),
                            article.getCategory(),
                            firstImageUrl,
                            article.getMember().getMemberId(),
                            article.getMember().getNickname()
                    );
                })
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회
    public ArticleDetailResponseDTO getArticleDetail(Long articleId, UserDetails userDetails) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.RESOURCE_NOT_FOUND.getMessage()));

        boolean myArticle = false;
        boolean myLike = false;

        if (userDetails != null) {
            Long userId = memberService.getUserIdByEmail(userDetails.getUsername());
            myLike = articleLikeRepository.findByArticleIdAndMemberMemberId(articleId, userId).isPresent();
            myArticle = article.getMember().getMemberId().equals(userId);
        }

        List<String> imageUrls = article.getImages().isEmpty() ? null :
                article.getImages().stream().map(ArticleImage::getImageUrl).collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss");
        String formattedDate = article.getUpdatedAt().format(formatter);

        return ArticleDetailResponseDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .date(formattedDate)
                .content(article.getContent())
                .likeCnt(article.getLikeCnt())
                .cmtCnt(article.getCmtCnt())
                .images(imageUrls)
                .category(article.getCategory())
                .myArticle(myArticle)
                .myLike(myLike)
                .authorId(article.getMember().getMemberId())
                .nickname(article.getMember().getNickname())
                .build();
    }

    // 내가 작성한 게시글 조회
    public List<MyArticleListResponseDTO> getMyArticleList(UserDetails userDetails){
        Long userId = memberService.getUserIdByEmail(userDetails.getUsername());

        List<Article> myMemos = articleRepository.findArticlesByMemberMemberId(userId);

        return myMemos.stream()
                .map(memo -> new MyArticleListResponseDTO(
                        memo.getId(),
                        memo.getTitle(),
                        memo.getContent(),
                        memo.getCategory(),
                        memo.getLikeCnt(),
                        memo.getImages().stream().map(ArticleImage::getImageUrl).collect(Collectors.toList())
                )).collect(Collectors.toList());
    }

    // 게시글 삭제
    public void deleteArticle(Long articleId, Long userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.RESOURCE_NOT_FOUND.getMessage()));

        if (!article.getMember().getMemberId().equals(userId)) {
            throw new NotFoundException(ErrorStatus.ARTICLE_WRITER_NOT_SAME_USER_EXCEPTION.getMessage());
        }

        // 게시글과 연관된 댓글 삭제
        List<Comment> comments = commentRepository.findByArticle(article);
        commentRepository.deleteAll(comments);

        // 이미지, 좋아요는 CascadeType.ALL로 Article 삭제 시 자동 제거
        articleRepository.delete(article);
    }

    // 게시글 수정
    public void updateArticle(Long memoId, Long userId, ArticleCreateRequestDTO articleRequest,
                              List<MultipartFile> newImages, List<String> deleteImageUrls) throws IOException {

        Article article = articleRepository.findById(memoId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.RESOURCE_NOT_FOUND.getMessage()));

        if (!article.getMember().getMemberId().equals(userId)) {
            throw new BadRequestException(ErrorStatus.ARTICLE_WRITER_NOT_SAME_USER_EXCEPTION.getMessage());
        }

        Article updatedArticle = Article.builder()
                .id(article.getId())
                .member(article.getMember())
                .title(articleRequest.getTitle())
                .content(articleRequest.getContent())
                .category(articleRequest.getCategory())
                .likeCnt(article.getLikeCnt())
                .cmtCnt(article.getCmtCnt())
                .images(new ArrayList<>(article.getImages()))
                .build();

        // 삭제할 이미지 처리
        if (deleteImageUrls != null && !deleteImageUrls.isEmpty()) {
            List<ArticleImage> imagesToRemove = article.getImages().stream()
                    .filter(image -> deleteImageUrls.contains(image.getImageUrl()))
                    .collect(Collectors.toList());

            for (ArticleImage image : imagesToRemove) {
                // S3에서 이미지 삭제
                s3Service.deleteFile(image.getImageUrl());
                updatedArticle.getImages().remove(image);
            }
        }

        // 새로운 이미지 업로드 및 추가
        if (newImages != null && !newImages.isEmpty()) {
            List<String> imageUrls = s3Service.uploadArticleImages(String.valueOf(userId), newImages);
            for (String url : imageUrls) {
                ArticleImage image = ArticleImage.builder()
                        .imageUrl(url)
                        .article(updatedArticle)
                        .build();
                updatedArticle.getImages().add(image);
            }
        }

        articleRepository.save(updatedArticle);
    }

    // 좋아요 토글
    public void toggleLike(Long articleId, Long userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.RESOURCE_NOT_FOUND.getMessage()));

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        Optional<ArticleLike> existingLike = articleLikeRepository.findByArticleIdAndMemberMemberId(articleId, userId);

        if (existingLike.isPresent()) {
            articleLikeRepository.delete(existingLike.get());
            article = article.decreaseLikeCnt();
        } else {
            ArticleLike articleLike = ArticleLike.builder()
                    .article(article)
                    .member(member)
                    .build();
            articleLikeRepository.save(articleLike);
            article = article.increaseLikeCnt();
        }

        articleRepository.save(article);
    }

    // 팔로우 하고 있는 사람의 게시글 리스트 조회
    public List<ArticleTotalListResponseDTO> getFollowingArticles(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        List<Follow> followingList = member.getFollowing();
        List<Long> followingIds = followingList.stream()
                .map(follow -> follow.getFollowing().getMemberId())
                .toList();

        if (followingIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Article> articles = articleRepository.findAll().stream()
                .filter(article -> followingIds.contains(article.getMember().getMemberId()))
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .toList();

        return articles.stream()
                .map(article -> {
                    String firstImageUrl = article.getImages().isEmpty()
                            ? null
                            : article.getImages().get(0).getImageUrl();

                    return new ArticleTotalListResponseDTO(
                            article.getId(),
                            article.getTitle(),
                            article.getCategory(),
                            firstImageUrl,
                            article.getMember().getMemberId(),
                            article.getMember().getNickname()
                    );
                })
                .collect(Collectors.toList());
    }

}
