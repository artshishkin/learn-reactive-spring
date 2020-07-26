package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxReactiveTest {

    private Flux<String> stringFlux;

    @BeforeEach
    void setUp() {
        stringFlux = Flux.just("Spring", "Spring Boot", "Reactive Spring").log();
    }

    @Test
    @DisplayName("test order of flux messages")
    void fluxTestElements_WithoutError() {
        StepVerifier
                .create(stringFlux)
                .expectNext("Spring")
                .expectNext("Spring Boot")
                .expectNext("Reactive Spring")
                .verifyComplete();  //<-Start the flux emitting
    }

    @Test
    @DisplayName("test order of flux messages inlined")
    void fluxTestElements_WithoutErrorInline() {
        StepVerifier
                .create(stringFlux)
                .expectNext("Spring", "Spring Boot", "Reactive Spring")
                .verifyComplete();
    }

    @Test
    @DisplayName("expect error")
    void fluxTestElements_WithError() {
        Flux<String> fluxWithException = stringFlux.concatWith(Flux.error(new RuntimeException("Exception occurred")));
        StepVerifier
                .create(fluxWithException)
                .expectNext("Spring")
                .expectNext("Spring Boot")
                .expectNext("Reactive Spring")
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("verify exception by class")
    void fluxTestElements_VerifyExceptionClass() {
        Flux<String> fluxWithException = stringFlux.concatWith(Flux.error(new RuntimeException("Exception occurred")));
        StepVerifier
                .create(fluxWithException)
                .expectNext("Spring")
                .expectNext("Spring Boot")
                .expectNext("Reactive Spring")
                .verifyError(RuntimeException.class);
    }

    @Test
    @DisplayName("verify exception by message")
    void fluxTestElements_VerifyExceptionMessage() {
        Flux<String> fluxWithException = stringFlux.concatWith(Flux.error(new RuntimeException("Exception occurred")));
        StepVerifier
                .create(fluxWithException)
                .expectNext("Spring")
                .expectNext("Spring Boot")
                .expectNext("Reactive Spring")
                .verifyErrorMessage("Exception occurred");
    }

    @Test
    @DisplayName("verify exception by message")
    void fluxTestElements_VerifyException() {
        Flux<String> fluxWithException = stringFlux.concatWith(Flux.error(new RuntimeException("Exception occurred")));
        StepVerifier
                .create(fluxWithException)
                .expectNext("Spring")
                .expectNext("Spring Boot")
                .expectNext("Reactive Spring")
                .verifyErrorMatches(e -> e.getMessage().equals("Exception occurred") && e instanceof RuntimeException);
    }

    @Test
    @DisplayName("expect Next Count")
    void fluxTestElements_ExpectNextCount() {
        Flux<String> fluxWithException = stringFlux.concatWith(Flux.error(new RuntimeException("Exception occurred")));
        StepVerifier
                .create(fluxWithException)
                .expectNextCount(3)
                .verifyErrorMatches(e -> e.getMessage().equals("Exception occurred") && e instanceof RuntimeException);
    }
}
