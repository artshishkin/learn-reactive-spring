package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

public class FluxStudy {
    @Test
    @DisplayName("Simple subscriber ends with onComplete()")
    void fluxTest() {
        Flux<String> stringFlux = Flux.just("Spring", "Spring Boot", "Reactive Spring").log();
        stringFlux.subscribe(System.out::println);
    }

    @Test
    @DisplayName("Flux that terminates by exception")
    void fluxTestWithError() {
        Flux<String> stringFlux = Flux
                .just("Spring", "Spring Boot", "Reactive Spring")
                .concatWith(Flux.error(new RuntimeException("Exception occurred")))
                .log();
        stringFlux.subscribe(
                System.out::println,
                (e) -> System.err.println(e)
        );
    }

    @Test
    @DisplayName("After error message flux will terminate")
    void fluxTestWithErrorAndThenNewElements() {
        Flux<String> stringFlux = Flux
                .just("Spring", "Spring Boot", "Reactive Spring")
                .concatWith(Flux.error(new RuntimeException("Exception occurred")))
                .concatWith(Flux.just("After Exception", "Will not print"))
                .log();
        stringFlux.subscribe(
                System.out::println,
                (e) -> System.err.println(e)
        );
    }

    @Test
    @DisplayName("After flux emits all messages there will be Completed message")
    void fluxTestWithOnCompleteEvent() {
        Flux<String> stringFlux = Flux
                .just("Spring", "Spring Boot", "Reactive Spring")
                .concatWith(Flux.just("After Exception", "Will not print"))
                .log();
        stringFlux.subscribe(
                System.out::println,
                (e) -> System.err.println(e),
                ()-> System.out.println("Completed")
        );
    }
}
