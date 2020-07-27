package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.util.concurrent.CountDownLatch;

import static java.time.Duration.ofMillis;

public class FluxAndMonoWithTimeTest {

    @Test
    void infiniteSequence() throws InterruptedException {
        Flux<Long> infiniteFlux = Flux.interval(ofMillis(100L)).log();
        infiniteFlux.subscribe(m -> System.out.println("value is " + m));

        Thread.sleep(1000);
    }

    @Test
    void limitedSequence_take_withCountdownLatch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Flux<Long> finiteFlux = Flux.interval(ofMillis(100L)).take(10).log();
        finiteFlux.subscribe(m -> System.out.println("value is " + m), null, latch::countDown);

        latch.await();
    }

    @Test
    void finiteSequence_delayElements() {
        Flux<Integer> finiteFlux = Flux
                .interval(ofMillis(10L))
                .log()
                .take(3)
                .map(Long::intValue)
                .delayElements(ofMillis(100))
                .log();

        StepVerifier.create(finiteFlux)
                .expectSubscription()
                .expectNext(0, 1, 2)
                .verifyComplete();
    }

    @Test
    void finiteSequence_delayElementsTake2Times() {
        Flux<Integer> finiteFlux = Flux
                .interval(ofMillis(10L))
                .log()
                .take(20)
                .map(Long::intValue)
                .delayElements(ofMillis(100))
                .take(3)
                .log();

        StepVerifier.create( finiteFlux)
                .expectSubscription()
                .expectNext(0, 1, 2)
                .verifyComplete();
    }

    @Nested
    @DisplayName("Testing with virtual timer")
    class WithVirtualTimer{
        @BeforeEach
        void setUp() {
            VirtualTimeScheduler.getOrSet();
        }

        @Test
        void finiteSequence_test() {
            Flux<Long> finiteFlux = Flux.interval(ofMillis(100L)).take(3).log();

            StepVerifier.withVirtualTime(() -> finiteFlux)
                    .expectSubscription()
                    .thenAwait(ofMillis(300))
                    .expectNext(0L, 1L, 2L)
                    .verifyComplete();
        }

        @Test
        void finiteSequence_map() {
            Flux<Integer> finiteFlux = Flux
                    .interval(ofMillis(100L))
                    .take(3)
                    .map(Long::intValue)
                    .log();

            StepVerifier.withVirtualTime(() -> finiteFlux)
                    .expectSubscription()
                    .thenAwait(ofMillis(300))
                    .expectNext(0, 1, 2)
                    .verifyComplete();
        }
    }
}
