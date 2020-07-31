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

    @GetMapping("illegalStateException")
    public Mono<Void> illegalStateException() {
        throw new IllegalStateException("Illegal State Exception occurred");
    }
    @GetMapping("arithmeticException")
    public Mono<Void> arithmeticException() {
        throw new ArithmeticException("Arithmetic Exception occurred");
    }

    @GetMapping("fileException")
    public Mono<Void> catchableException() throws FileNotFoundException {
        throw new FileNotFoundException("File Not Found Exception occurred");
    }

    @GetMapping("runtimeException")
    public Mono<Void> runtimeException() {
        throw new RuntimeException("Runtime Exception occurred");
    }

    @ExceptionHandler(ArithmeticException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String arithmeticExceptionHandler(RuntimeException exception) {
        log.error("ArithmeticException caught in arithmeticExceptionHandler:", exception);
        return exception.getMessage();
    }
}
