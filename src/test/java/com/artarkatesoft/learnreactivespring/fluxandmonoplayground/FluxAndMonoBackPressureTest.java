package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxAndMonoBackPressureTest {
    @Test
    void backPressureTest() {
        Flux<Integer> flux = Flux.range(201, 10).log();
        StepVerifier.create(flux)
                .expectSubscription()
                .thenRequest(1)
                .expectNext(201)
                .thenRequest(2)
                .expectNext(202, 203)
                .thenCancel()
                .verify();
    }

    @Test
    void backPressureImpl_requestN() {
        Flux<Integer> flux = Flux.range(201, 10).log();
        flux.subscribe(
                element -> System.out.println("Element is " + element),
                (exc) -> System.err.println(exc),
                () -> System.out.println("Complete"),
                subscription -> subscription.request(3));
    }

    @Test
    void backPressureImpl_cancel() {
        Flux<Integer> flux = Flux.range(201, 10).log();
        flux.subscribe(
                element -> System.out.println("Element is " + element),
                (exc) -> System.err.println(exc),
                () -> System.out.println("Complete"),
                subscription -> subscription.cancel());
    }

    @Test
    void customizedBackPressure_takeAllByOne() {
        Flux<Integer> flux = Flux.range(201, 10).log();
        flux.subscribe(
                new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnNext(Integer value) {
                        System.out.println("Element is " + value);
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }

                        if (!isDisposed())
                            upstream().request(1);
                    }

                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        subscription.request(1);
                    }

                    @Override
                    protected void hookOnComplete() {
                        System.out.println("Complete");
                    }
                }

        );
    }

    @Test
    void customizedBackPressure_takeByOne_limitByDispose() {
        Flux<Integer> flux = Flux.range(201, 10).log();
        flux.subscribe(
                new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnNext(Integer value) {
                        System.out.println("Element is " + value);
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        if (value.equals(206)) dispose();

                        if (!isDisposed())
                            upstream().request(1);
                    }
                }

        );
    }
}
