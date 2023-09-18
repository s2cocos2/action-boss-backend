package com.sparta.actionboss.global.exception.handler;

import com.sparta.actionboss.global.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j(topic = "error")
public class CustomExceptionHandler {

    @ExceptionHandler(SignupException.class)
    public ResponseEntity<?> signupExceptionHandler(SignupException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getMsg()));
    }

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<?> loginExceptionHandler(LoginException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getMsg()));
    }

    @ExceptionHandler(AgreeException.class)
    public ResponseEntity<?> agreeExceptionHandler(AgreeException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getMsg()));
    }

    @ExceptionHandler(PostException.class)
    public ResponseEntity<?> postExceptionHandler(PostException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getMsg()));
    }

    @ExceptionHandler(ImageException.class)
    public ResponseEntity<?> postExceptionHandler(ImageException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getMsg()));
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<?> postExceptionHandler(S3Exception e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getMsg()));
    }

    @ExceptionHandler(CommentException.class)
    public ResponseEntity<?> postExceptionHandler(CommentException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getMsg()));
    }

    @ExceptionHandler(SearchException.class)
    public ResponseEntity<?> searchExceptionHandler(SearchException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getMsg()));
    }

    @ExceptionHandler(DoneException.class)
    public ResponseEntity<?> searchExceptionHandler(DoneException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getMsg()));
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<?> searchExceptionHandler(UserException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getMsg()));
    }

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<?> searchExceptionHandler(NotificationException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getMsg()));
    }

    @ExceptionHandler(MyPageException.class)
    public ResponseEntity<?> tokenExceptionHandler(MyPageException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getMsg()));
    }
}
