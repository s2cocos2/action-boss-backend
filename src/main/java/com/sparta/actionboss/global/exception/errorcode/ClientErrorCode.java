package com.sparta.actionboss.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClientErrorCode {


    // User
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "이미 존재하는 닉네임입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    EMAIL_AUTHENTICATION_FAILED(HttpStatus.BAD_REQUEST, "이메일 인증에 실패하였습니다."),
    EMAIL_SENDING_FAILED(HttpStatus.BAD_REQUEST, "이메일 인증 코드를 보내지 못했습니다."),
    INVALID_ADMIN_TOKEN(HttpStatus.BAD_REQUEST, "관리자 암호가 일치하지 않습니다."),
    SIGNUP_FAILED(HttpStatus.BAD_REQUEST, "회원가입에 실패하였습니다."),

    NO_ACCOUNT(HttpStatus.NOT_FOUND, "가입되지 않은 계정입니다."),
    INVALID_PASSWORDS(HttpStatus.UNAUTHORIZED, "잘못된 비밀번호 입니다."),

    INVALID_REFRESHTOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 refresh token 입니다."),
    NO_REFRESHTOKEN(HttpStatus.BAD_REQUEST, "refresh token이 존재하지 않습니다."),


    NO_AGREE(HttpStatus.NOT_FOUND, "동의해요에 대한 정보가 존재하지 않습니다."),
    NO_DONE(HttpStatus.NOT_FOUND, "해결됐어요에 대한 정보가 존재하지 않습니다."),

    // Post
    UPLOAD_NO_IMAGE(HttpStatus.BAD_REQUEST, "이미지를 1장 이상 업로드 해주세요."),
    UPLOAD_MAXIMUM_IMAGE(HttpStatus.BAD_REQUEST, "최대 3장의 이미지만 업로드할 수 있습니다."),
    ALREADY_DONE_POST(HttpStatus.I_AM_A_TEAPOT, "이미 완료된 민원글입니다."),
    NO_PERMISSION_UPDATE(HttpStatus.FORBIDDEN, "이 게시글을 변경할 수 있는 권한이 없습니다."),
    NO_PERMISSION_DELETE(HttpStatus.FORBIDDEN, "이 게시글을 삭제할 수 있는 권한이 없습니다."),
    NO_POST(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    NON_LOGIN_CREATE(HttpStatus.FORBIDDEN, "로그인 후 게시글을 작성할 수 있습니다."),

    NO_IMAGE(HttpStatus.NOT_FOUND, "존재하지 않는 이미지입니다."),
    ONLY_PERMIT_IMAGE(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있습니다."),

    // Search
    SEARCH_NULL(HttpStatus.BAD_REQUEST, "검색어가 없습니다."),
    SEARCH_NOT_FOUND(HttpStatus.NOT_FOUND, "검색 결과가 없습니다."),

    NO_COMMENT(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    NO_PERMISSION_COMMENT_DELETE(HttpStatus.FORBIDDEN, "이 댓글을 삭제할 수 있는 권한이 없습니다."),

    // Notification
    NO_NOTIFICATION(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),
    NO_REMISSION_READ(HttpStatus.FORBIDDEN, "해당 알림을 읽을 수 있는 권한이 없습니다."),

    // Mypage
    REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "등록된 이메일이 있습니다.")

    ;
    private final HttpStatus statusCode;
    private final String msg;

}
