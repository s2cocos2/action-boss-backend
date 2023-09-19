package com.sparta.actionboss.global.exception.handler;

import com.sparta.actionboss.global.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j(topic = "error")
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<?> agreeExceptionHandler(CommonException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getMsg()));
    }
}
