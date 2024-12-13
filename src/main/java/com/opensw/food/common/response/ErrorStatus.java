package com.opensw.food.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)

public enum ErrorStatus {
    /**
     * 400 BAD_REQUEST
     */
    VALIDATION_REQUEST_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 값이 입력되지 않았습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 비밀번호입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    NOT_ALLOW_IMG_MIME(HttpStatus.BAD_REQUEST,"이미지 확장자 업로드만 가능합니다"),
    ARTICLE_WRITER_NOT_SAME_USER_EXCEPTION(HttpStatus.BAD_REQUEST,"게시글 작성자와 요청자가 다릅니다."),
    COMMENT_WRITER_NOT_SAME_USER_EXCEPTION(HttpStatus.BAD_REQUEST,"댓글 작성자와 요청자가 다릅니다."),

    /**
     * 401 UNAUTHORIZED
     */
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"인증되지 않은 사용자입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원되지 않는 토큰입니다."),
    EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 비어있습니다."),

    /**
     * 403 FORBIDDEN
     */
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    /**
     * 404 NOT_FOUND
     */
    USER_NOTFOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    PARENT_COMMENT_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND,"부모 댓글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND,"댓글을 찾을 수 없습니다."),

    /**
     * 500 SERVER_ERROR
     */
    FAIL_UPLOAD_PROFILE_IMAGE(HttpStatus.INTERNAL_SERVER_ERROR, "프로필 사진이 변경되지 않았습니다."),


    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
