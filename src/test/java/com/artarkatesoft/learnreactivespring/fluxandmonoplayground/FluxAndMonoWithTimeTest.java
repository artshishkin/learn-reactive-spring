package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public class FluxAndMonoWithTimeTest {
    @Test
    void infiniteSequence() throws InterruptedException {
        Flux<Long> infiniteFlux = Flux.interval(Duration.ofMillis(100L)).log();
        infiniteFlux.subscribe(m -> System.out.println("value is " + m));

        Thread.sleep(3000);
    }

    @Test
    void limitedSequence_take_withCountdownLatch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Flux<Long> finiteFlux = Flux.interval(Duration.ofMillis(100L)).take(20).log();
        finiteFlux.subscribe(m -> System.out.println("value is " + m), null, latch::countDown);

        latch.await();
    }

    @Test
    void finiteSequence_test() {
        Flux<Long> finiteFlux = Flux.interval(Duration.ofMillis(100L)).take(3).log();

        StepVerifier.create(finiteFlux)
                .expectSubscription()
                .expectNext(0L, 1L, 2L)
                .verifyComplete();

    }

    @Test
    void finiteSequence_map() {
        Flux<Integer> finiteFlux = Flux
                .interval(Duration.ofMillis(100L))
                .take(3)
                .map(Long::intValue)
                .log();

        StepVerifier.create(finiteFlux)
                .expectSubscription()
                .expectNext(0, 1, 2)
                .verifyComplete();

    }
    @Test
    void finiteSequence_delayElements() {
        Flux<Integer> finiteFlux = Flux
                .interval(Duration.ofMillis(100L))
                .log()
                .take(3)
                .map(Long::intValue)
                .delayElements(Duration.ofSeconds(1))
                .log();

        StepVerifier.create(finiteFlux)
                .expectSubscription()
                .expectNext(0, 1, 2)
                .verifyComplete();

    }

    @Test
    void finiteSequence_delayElementsTake2Times() {
        Flux<Integer> finiteFlux = Flux
                .interval(Duration.ofMillis(100L))
                .log()
                .take(20)
                .map(Long::intValue)
                .delayElements(Duration.ofSeconds(1))
                .take(3)
                .log();

        StepVerifier.create(finiteFlux)
                .expectSubscription()
                .expectNext(0, 1, 2)
                .verifyComplete();

    }
}
