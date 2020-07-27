package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

public class FluxAndMonoTransformTest {

    List<String> names = Arrays.asList("art", "kate", "arina", "nazar");

    @Test
    void fluxTransformUsingMap() {
        Flux<String> stringFlux = Flux
                .fromIterable(names)
                .log()
                .map(String::toUpperCase)
                .log();
        StepVerifier.create(stringFlux)
                .expectNext("ART", "KATE", "ARINA", "NAZAR")
                .verifyComplete();
    }

    @Test
    void fluxTransformUsingMap_Length() {
        Flux<Integer> integerFlux = Flux
                .fromIterable(names)
                .log()
                .map(String::length)
                .log();
        StepVerifier.create(integerFlux)
                .expectNext(3, 4, 5, 5)
                .verifyComplete();
    }

    @Test
    void fluxTransform_Repeat() {

        int numRepeat = 3;
        int totalNumExecution = numRepeat + 1;
        Flux<Integer> flux = Flux
                .range(3, 5)
                .repeat(numRepeat)
                .log();
        StepVerifier.create(flux)
                .expectNext(3, 4, 5, 6, 7, 3)
                .expectNextCount(totalNumExecution * 5 - 6)
                .verifyComplete();
    }

    @Test
    void fluxTransform_RepeatFilter() {

        int numRepeat = 3;
        int totalNumExecution = numRepeat + 1;
        Flux<Integer> flux = Flux
                .range(3, 5)
                .repeat(numRepeat)
                .filter(i -> i % 2 == 1)
                .log();
        StepVerifier.create(flux)
                .expectNext(3, 5, 7, 3)
                .expectNextCount(totalNumExecution * 3 - 4)
                .verifyComplete();
    }
}
