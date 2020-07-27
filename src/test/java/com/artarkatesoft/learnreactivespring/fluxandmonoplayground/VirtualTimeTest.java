package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;

public class VirtualTimeTest {
    @Test
    void testingWithoutVirtualTime() {
        Flux<Long> flux = Flux.interval(Duration.ofSeconds(1)).take(3);

        StepVerifier.create(flux.log())
                .expectNext(0L, 1L, 2L)
                .verifyComplete();
    }

    @Test
    void testingWithVirtualTime() {
        VirtualTimeScheduler.getOrSet();

        Flux<Long> flux = Flux.interval(Duration.ofSeconds(1)).take(3).log();

        StepVerifier.withVirtualTime(() -> flux)
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(3))
                .expectNext(0L, 1L, 2L)
                .verifyComplete();
    }
}
