package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.time.Duration;

public class FluxAndMonoErrorTest {

    @Test
    void flux_withError() {
        Flux<String> flux = Flux
                .just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Error occurred")))
                .concatWith(Flux.just("Unreachable"));
        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("A", "B", "C")
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void fluxErrorHandling_onErrorResume() {
        Flux<String> flux = Flux
                .just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Error occurred")))
                .concatWith(Flux.just("Unreachable"))
                .onErrorResume((e) -> {
                    System.out.println("Exception is " + e);
                    return Flux.just("errorHandling1", "errorHandling2");
                });
        StepVerifier.create(flux.log())
                .expectSubscription()
                .expectNext("A", "B", "C")
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void fluxErrorHandling_onErrorReturn() {
        String fallbackValue = "Was Error";
        Flux<String> flux = Flux
                .just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Error occurred")))
                .concatWith(Flux.just("Unreachable"))
                .onErrorReturn(fallbackValue);
        StepVerifier.create(flux.log())
                .expectSubscription()
                .expectNext("A", "B", "C")
                .expectNext(fallbackValue)
                .verifyComplete();
    }

    @Test
    void fluxErrorHandling_onErrorMap() {

        Flux<String> flux = Flux
                .just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Error occurred")))
                .concatWith(Flux.just("Unreachable"))
                .onErrorMap(CustomException::new);
        StepVerifier.create(flux.log())
                .expectSubscription()
                .expectNext("A", "B", "C")
                .verifyErrorMessage("Error occurred");
    }

    @Test
    void fluxErrorHandling_onErrorRetry() {

        Flux<String> flux = Flux
                .just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Error occurred")))
                .concatWith(Flux.just("Unreachable"))
                .onErrorMap(CustomException::new)
                .retry(2);

        StepVerifier.create(flux.log())
                .expectSubscription()
                .expectNext("A", "B", "C")
                .expectNext("A", "B", "C")
                .expectNext("A", "B", "C")
                .verifyErrorMessage("Error occurred");
    }

    @Test
    @DisplayName("retryBackoff is deprecated, so we need to use retryWhen ")
    void fluxErrorHandling_onErrorRetryWhen() {

        Flux<String> flux = Flux
                .just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Error occurred")))
                .concatWith(Flux.just("Unreachable"))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(100)));

        StepVerifier.create(flux.log())
                .expectSubscription()
                .expectNext("A", "B", "C")
                .expectNext("A", "B", "C")
                .expectNext("A", "B", "C")
                .verifyErrorMatches(e -> e.getMessage().contains("Retries exhausted"));
    }
}
