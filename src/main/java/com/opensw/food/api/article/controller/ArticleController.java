package com.opensw.food.api.article.controller;

import com.opensw.food.api.article.dto.ArticleCreateRequestDTO;
import com.opensw.food.api.article.dto.ArticleDetailResponseDTO;
import com.opensw.food.api.article.dto.ArticleTotalListResponseDTO;
import com.opensw.food.api.article.dto.MyArticleListResponseDTO;
import com.opensw.food.api.article.service.ArticleService;
import com.opensw.food.api.member.service.MemberService;
import com.opensw.food.common.exception.BadRequestException;
import com.opensw.food.common.response.ApiResponse;
import com.opensw.food.common.response.ErrorStatus;
import com.opensw.food.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Article", description = "게시글 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/article")
public class ArticleController {

    private final MemberService memberService;
    private final ArticleService articleService;

    @Operation(
            summary = "게사글 등록 API",
            description = "새로운 게시글을 등록합니다. with MultipartFile"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 정보가 입력되지 않았습니다."),
    })
    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> createArticle(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") String category,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {

        // 필수 입력 값 누락 체크
        if (isNullOrEmpty(title) ||
                isNullOrEmpty(content) ||
                isNullOrEmpty(category)) {
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION.getMessage());
        }

        // 이미지 파일 검증
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (!isImageFile(image)) {
                    throw new BadRequestException(ErrorStatus.NOT_ALLOW_IMG_MIME.getMessage());
                }
            }
        }

        ArticleCreateRequestDTO articleCreateRequestDTO = ArticleCreateRequestDTO.builder()
                .title(title)
                .content(content)
                .category(category)
                .build();

        Long userId = memberService.getUserIdByEmail(userDetails.getUsername());

        articleService.createArticle(userId, articleCreateRequestDTO, images);
        return ApiResponse.success_only(SuccessStatus.CREATE_ARTICLE_SUCCESS);
    }

    @Operation(
            summary = "전체 게시글 조회 API",
            description = "현재 게시글 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 정보가 입력되지 않았습니다."),
    })
    @GetMapping("/total")
    public ResponseEntity<ApiResponse<List<ArticleTotalListResponseDTO>>> getTotalArticle(){

        List<ArticleTotalListResponseDTO> articles = articleService.getTotalArticle();
        return ApiResponse.success(SuccessStatus.SEND_ARTICLE_SUCCESS, articles);
    }

    @Operation(
            summary = "게시글 상세 조회 API",
            description = "특정 게시글의 상세 정보를 조회합니다. / 비 로그인 상태이면 토큰을 안넘기고, 로그인상태이면 엑세스토큰을 넘겨줘야합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 정보가 입력되지 않았습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 게시글을 찾을 수 없습니다."),
    })
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<ArticleDetailResponseDTO>> getMemoDetail(
            @RequestParam Long articleId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 필수 입력 값 누락 체크
        if (articleId == null) {
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION.getMessage());
        }

        ArticleDetailResponseDTO articleDetail = articleService.getArticleDetail(articleId, userDetails);
        return ApiResponse.success(SuccessStatus.SEND_ARTICLE_SUCCESS, articleDetail);
    }

    @Operation(
            summary = "내 게시글 조회 API",
            description = "내가 작성한 메모를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
    })
    @GetMapping("/my-article")
    public ResponseEntity<ApiResponse<List<MyArticleListResponseDTO>>> getMyMemo(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<MyArticleListResponseDTO> myArticleList = articleService.getMyArticleList(userDetails);
        return ApiResponse.success(SuccessStatus.SEND_ARTICLE_SUCCESS, myArticleList);
    }

    @Operation(
            summary = "게시글 삭제 API",
            description = "등록한 게시글을 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "게시글 작성자와 삭제 요청자가 다릅니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다.")
    })
    @DeleteMapping("/delete/{articleId}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(
            @PathVariable Long articleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // 필수 입력 값 누락 체크
        if (articleId == null) {
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION.getMessage());
        }

        Long userId = memberService.getUserIdByEmail(userDetails.getUsername());
        articleService.deleteArticle(articleId, userId);

        return ApiResponse.success_only(SuccessStatus.DELETE_MEMO_SUCCESS);
    }

    @Operation(
            summary = "게시글 수정 API",
            description = "기존 게시글을 수정합니다. 삭제할 이미지 URL과 새로운 이미지를 함께 처리합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 정보가 입력되지 않았습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 게시글을 찾을 수 없습니다."),
    })
    @PutMapping(value = "/modify/{articleId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateArticle(
            @PathVariable Long articleId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") String category,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "deleteImageUrls", required = false) List<String> deleteImageUrls
    ) throws IOException {

        // 필수 입력 값 누락 체크
        if (isNullOrEmpty(title) ||
                isNullOrEmpty(content) ||
                isNullOrEmpty(category) ||
                articleId == null) {
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION.getMessage());
        }

        // 이미지 파일 검증
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (!isImageFile(image)) {
                    throw new BadRequestException(ErrorStatus.NOT_ALLOW_IMG_MIME.getMessage());
                }
            }
        }

        ArticleCreateRequestDTO articleUpdateRequestDTO = ArticleCreateRequestDTO.builder()
                .title(title)
                .content(content)
                .category(category)
                .build();

        Long userId = memberService.getUserIdByEmail(userDetails.getUsername());

        articleService.updateArticle(articleId, userId, articleUpdateRequestDTO, images, deleteImageUrls);

        return ApiResponse.success_only(SuccessStatus.UPDATE_MEMO_SUCCESS);
    }


    private boolean isImageFile(MultipartFile file) {
        // 허용되는 이미지 MIME 타입
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/bmp") ||
                        contentType.equals("image/webp")
        );
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

}
