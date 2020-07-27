package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

public class FluxAndMonoFilterTest {

    List<String> names = Arrays.asList("art", "kate", "arina", "nazar");

    @Test
    void filterStartLetterA() {
        Flux<String> stringFlux = Flux
                .fromIterable(names)
                .filter(s -> s.startsWith("a"));
        StepVerifier.create(stringFlux)
                .expectNext("art")
                .expectNext("arina")
                .verifyComplete();

    }
    @Test
    void filterNameLength() {
        Flux<String> stringFlux = Flux
                .fromIterable(names)
                .filter(s -> s.length()==5);
        StepVerifier.create(stringFlux)
                .expectNext("arina","nazar")
                .verifyComplete();

    }
}
