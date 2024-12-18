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
import com.opensw.food.api.comment.repository.CommentRepository;
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

        // 게시글에 연관된 댓글 삭제
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

        // 기존 이미지 목록 복사
        List<ArticleImage> oldImages = new ArrayList<>(article.getImages());

        // 업데이트된 게시글 객체 생성 (일단 기존 이미지 그대로)
        Article updatedArticle = Article.builder()
                .id(article.getId())
                .member(article.getMember())
                .title(articleRequest.getTitle())
                .content(articleRequest.getContent())
                .category(articleRequest.getCategory())
                .likeCnt(article.getLikeCnt())
                .cmtCnt(article.getCmtCnt())
                .images(new ArrayList<>(oldImages))
                .build();

        // 1. Base64 인라인 이미지 처리
        List<String> newBase64ImageUrls = processBase64Images(articleRequest.getContent(), String.valueOf(userId));
        updatedArticle.addImages(newBase64ImageUrls);

        // 2. deleteImageUrls에 해당하는 이미지 제거
        if (deleteImageUrls != null && !deleteImageUrls.isEmpty()) {
            List<ArticleImage> imagesToRemove = updatedArticle.getImages().stream()
                    .filter(image -> deleteImageUrls.contains(image.getImageUrl()))
                    .collect(Collectors.toList());

            for (ArticleImage img : imagesToRemove) {
                s3Service.deleteFile(img.getImageUrl());
                updatedArticle.getImages().remove(img);
            }
        }

        // 3. 새로운 Multipart 이미지 업로드
        List<String> newUploadedImageUrls = new ArrayList<>();
        if (newImages != null && !newImages.isEmpty()) {
            newUploadedImageUrls = s3Service.uploadArticleImages(String.valueOf(userId), newImages);
            for (String url : newUploadedImageUrls) {
                ArticleImage newImg = ArticleImage.builder()
                        .imageUrl(url)
                        .article(updatedArticle)
                        .build();
                updatedArticle.getImages().add(newImg);
            }
        }

        // 4. 기존 이미지 중 이번 요청에 포함되지 않은 이미지 제거
        // 최종 남아야 할 이미지 = newBase64ImageUrls + newUploadedImageUrls 중 deleteImageUrls에 없는 것들
        Set<String> finalImageSet = new HashSet<>();
        finalImageSet.addAll(newBase64ImageUrls);
        finalImageSet.addAll(newUploadedImageUrls);
        if (deleteImageUrls != null) {
            // 이미 삭제한 이미지들은 finalImageSet에 들어있어도 제거 대상
            finalImageSet.removeAll(deleteImageUrls);
        }

        // updatedArticle.getImages()를 순회하며 finalImageSet에 없는 이미지 제거
        List<ArticleImage> finalImages = new ArrayList<>();
        for (ArticleImage img : updatedArticle.getImages()) {
            if (finalImageSet.contains(img.getImageUrl())) {
                finalImages.add(img);
            } else {
                // finalImageSet에 없다면 제거
                s3Service.deleteFile(img.getImageUrl());
            }
        }

        // 정리된 이미지 리스트로 세팅
        updatedArticle = updatedArticle.toBuilder()
                .images(finalImages)
                .build();

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
                .filter(a -> followingIds.contains(a.getMember().getMemberId()))
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .toList();

        return articles.stream()
                .map(a -> {
                    String firstImageUrl = a.getImages().isEmpty()
                            ? null
                            : a.getImages().get(0).getImageUrl();

                    return new ArticleTotalListResponseDTO(
                            a.getId(),
                            a.getTitle(),
                            a.getCategory(),
                            firstImageUrl,
                            a.getMember().getMemberId(),
                            a.getMember().getNickname()
                    );
                })
                .collect(Collectors.toList());
    }
}
