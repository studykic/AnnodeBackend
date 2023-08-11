package com.ikchi.annode.Exception;

import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e) {
        e.printStackTrace();
        return "오류가 발생했습니다";
    }

    //  객체 바인딩시 예외는 크게 3가지가있다
    //  1. BindException : @ModelAttribute, @RequestParam, @RequestHeader를 사용하여 바인딩시 예외
    //  2. MethodArgumentNotValidException : @RequestBody를 사용하여 바인딩시 예외
    //  3. ConstraintViolationException : @Validated를 사용하여 바인딩시 예외

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();

        e.printStackTrace();
        return errorMessage;
    }


    // form-data 형식의 객체 필드 검사에 BindException이 사용된다
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public String handleValidationException(BindException e) {
        // 예외에서 에러 메시지를 가져옵니다.
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();

        e.printStackTrace();
        return errorMessage;
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UsernameNotFoundException.class)
    public String handleUsernameNotFoundException(BindException e) {
        // 예외에서 에러 메시지를 가져옵니다.
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();

        e.printStackTrace();
        return errorMessage;
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public String handleConstraintViolationException(ConstraintViolationException e) {

        String errorMessage = e.getConstraintViolations().iterator().next().getMessage();

        e.printStackTrace();
        return errorMessage;
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e) {
        e.printStackTrace();
        return e.getMessage();
    }


    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(NoSuchElementException e) {
        e.printStackTrace();
        return e.getMessage();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException e) {
        e.printStackTrace();
        return e.getMessage();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IOException.class)
    public String handleIOException(IOException e) {
        e.printStackTrace();
        return e.getMessage();
    }
}
