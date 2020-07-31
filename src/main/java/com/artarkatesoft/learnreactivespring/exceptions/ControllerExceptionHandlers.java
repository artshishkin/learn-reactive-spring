package com.artarkatesoft.learnreactivespring.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandlers {

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Order(10)
    public String illegalStateExceptionHandler(IllegalStateException exception) {
        log.error("IllegalStateException caught in ExceptionControllerAdvice:", exception);
        return exception.getMessage();
    }
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Order(20)
    public String runtimeExceptionHandler(RuntimeException exception) {
        log.error("RuntimeException caught in ExceptionControllerAdvice:", exception);
        return exception.getMessage();
    }

//    @ExceptionHandler
//    public Mono<ResponseEntity<String>> runtimeExceptionHandler(RuntimeException exception) {
//        return Mono.just(exception.getMessage())
//                .map(message -> ResponseEntity
//                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(message));
//    }

//    @ExceptionHandler(RuntimeException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public Mono<String> runtimeExceptionHandler(RuntimeException exception) {
//        log.error("Exception caught in runtimeExceptionHandler: ", exception);
//        return Mono.just(exception.getMessage());
//    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Order
    public String genericExceptionHandler(Exception exception) {
        log.error("Exception caught in ExceptionControllerAdvice:", exception);
        return exception.getMessage();
    }
}
