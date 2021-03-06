package com.artarkatesoft.learnreactivespring.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ItemSampleExceptionsHandler {

    static final String RUNTIME_EXCEPTION_MESSAGE = "Functional Runtime Exception occurred";

    public Mono<ServerResponse> runtimeException(ServerRequest request){
        log.info("in ItemSampleExceptionsHandler.runtimeException()");
        throw new RuntimeException(RUNTIME_EXCEPTION_MESSAGE);
    }
}
