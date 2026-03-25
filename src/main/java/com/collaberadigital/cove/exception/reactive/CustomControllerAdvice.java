package com.collaberadigital.cove.exception.reactive;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomControllerAdvice {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponse> handleCustomErrorExceptionsUserStatus(Exception e) {
//        HttpStatus customHttpStatus = HttpStatus.valueOf(451);
//        if (e instanceof InternalAuthenticationServiceException){
//            return new ResponseEntity<>(new ExceptionResponse("451",e.getMessage()), customHttpStatus);
//
//        }
        CustomException errorException = (CustomException) e;
        HttpStatus customHttpStatus = HttpStatus.valueOf(Integer.valueOf(errorException.getStatus()));
        return new ResponseEntity<>(new ExceptionResponse(errorException.getStatus(),errorException.getTitle(),errorException.getStatusType(),errorException.getMessage()), customHttpStatus);
    }
}
