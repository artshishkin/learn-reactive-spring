package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;

public class FluxAndMonoCombineTest {

    @Test
    void combineUsingMerge() {
        Flux<String> flux1 = Flux.just("A", "B", "C");
        Flux<String> flux2 = Flux.just("1", "2", "3");
        Flux<String> mergeFlux = Flux.merge(flux1, flux2);
        StepVerifier.create(mergeFlux.log())
                .expectSubscription()
                .expectNextCount(6)
                .verifyComplete();
    }

    @Test
    void combineUsingMerge_withDelay() {
        VirtualTimeScheduler.getOrSet();
        Flux<String> flux1 = Flux.just("A", "B", "C").delayElements(Duration.ofSeconds(1));
        Flux<String> flux2 = Flux.just("1", "2", "3").delayElements(Duration.ofSeconds(1));
        Flux<String> mergeFlux = Flux.merge(flux1, flux2).log();
        StepVerifier.withVirtualTime(() -> mergeFlux)
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(3))
                .expectNextCount(6)
                .verifyComplete();
    }

    @Test
    void combineUsingConcat() {
        Flux<String> flux1 = Flux.just("A", "B", "C");
        Flux<String> flux2 = Flux.just("1", "2", "3");
        Flux<String> concatFlux = Flux.concat(flux1, flux2);
        StepVerifier.create(concatFlux.log())
                .expectSubscription()
                .expectNext("A", "B", "C", "1", "2", "3")
                .verifyComplete();
    }

    @Test
    void combineUsingConcat_withDelay() {
        VirtualTimeScheduler.getOrSet();

        Flux<String> flux1 = Flux.just("A", "B", "C").delayElements(Duration.ofSeconds(1)).log();
        Flux<String> flux2 = Flux.just("1", "2", "3").delayElements(Duration.ofSeconds(1)).log();
        Flux<String> concatFlux = Flux.concat(flux1, flux2);
        StepVerifier.withVirtualTime(() -> concatFlux)
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(6))
                .expectNext("A", "B", "C", "1", "2", "3")
                .verifyComplete();
    }

    @Test
    void combineUsingZip() {
        Flux<String> flux1 = Flux.just("A", "B", "C");
        Flux<String> flux2 = Flux.just("1", "2", "3");

        Flux<String> zipFlux = Flux.zip(flux1, flux2, (t1, t2) -> t1.concat(t2));

        StepVerifier.create(zipFlux.log())
                .expectSubscription()
                .expectNext("A1", "B2", "C3")
                .verifyComplete();
    }

}
