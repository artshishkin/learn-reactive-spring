package com.artarkatesoft.learnreactivespring.controllers.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;

import static com.artarkatesoft.learnreactivespring.constants.ItemConstants.ITEM_END_POINT_V1;

@RestController
@RequestMapping(ITEM_END_POINT_V1)
@Slf4j
public class ItemSampleExceptionController {

    static final String RUNTIME_EXCEPTION_MESSAGE = "Runtime Exception occurred";
    static final String ARITHMETIC_EXCEPTION_MESSAGE = "Arithmetic Exception occurred";
    static final String ILLEGAL_STATE_EXCEPTION_MESSAGE = "Illegal State Exception occurred";
    static final String FILE_NOT_FOUND_EXCEPTION_MESSAGE = "File Not Found Exception occurred";

    @GetMapping("illegalStateException")
    public Mono<Void> illegalStateException() {
        throw new IllegalStateException(ILLEGAL_STATE_EXCEPTION_MESSAGE);
    }

    @GetMapping("arithmeticException")
    public Mono<Void> arithmeticException() {
        throw new ArithmeticException(ARITHMETIC_EXCEPTION_MESSAGE);
    }

    @GetMapping("fileException")
    public Mono<Void> catchableException() throws FileNotFoundException {
        throw new FileNotFoundException(FILE_NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @GetMapping("runtimeException")
    public Mono<Void> runtimeException() {
        throw new RuntimeException(RUNTIME_EXCEPTION_MESSAGE);
    }

    @ExceptionHandler(ArithmeticException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String arithmeticExceptionHandler(RuntimeException exception) {
        log.error("ArithmeticException caught in arithmeticExceptionHandler:", exception);
        return exception.getMessage();
    }
}
