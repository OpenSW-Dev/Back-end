package com.opensw.food.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {

    /**
     * 200
     */
    SEND_LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
    GET_MEMBER_SUCCESS(HttpStatus.OK, "회원 정보 조회 성공"),
    GET_CURRENT_MEMBER_SUCCESS(HttpStatus.OK, "현재 사용자 정보 조회 성공"),
    SEND_ARTICLE_SUCCESS(HttpStatus.OK,"게시글 조회 성공"),
    DELETE_MEMO_SUCCESS(HttpStatus.OK,"게시글 삭제 성공"),
    UPDATE_MEMO_SUCCESS(HttpStatus.OK,"게시글 수정 성공"),

    /**
     * 201
     */
    CREATE_ARTICLE_SUCCESS(HttpStatus.CREATED, "게시판 등록 성공"),
    SEND_SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입 성공"),

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}