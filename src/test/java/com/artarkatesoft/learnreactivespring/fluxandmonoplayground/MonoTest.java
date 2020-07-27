package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class MonoTest {
    @Test
    @DisplayName("Simple Mono verify Complete")
    void monoTest_WithoutError() {
        Mono<String> stringMono = Mono.just("Spring");
        StepVerifier.create(stringMono.log())
                .expectNext("Spring")
                .verifyComplete();
    }


    @Test
    @DisplayName("expect error")
    void monoTest_Error() {
        Mono<Object> monoError = Mono.error(() -> new RuntimeException("Error occurred")).log();
        StepVerifier.create(monoError)
                .verifyErrorMatches(e -> e.getMessage().equals("Error occurred") && e.getClass().equals(RuntimeException.class));
    }
}
