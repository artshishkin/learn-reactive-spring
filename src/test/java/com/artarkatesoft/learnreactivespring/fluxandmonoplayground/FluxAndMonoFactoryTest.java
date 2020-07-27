package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FluxAndMonoFactoryTest {

    List<String> stringList;

    @BeforeEach
    void setUp() {
        stringList = Arrays.asList("One", "Two", "Three");
    }

    @Test
    void fluxUsingIterable() {
        Flux<String> stringFlux = Flux.fromIterable(stringList);
        StepVerifier.create(stringFlux.log())
                .expectNextCount(3)
                .expectComplete()
                .verify();
    }

    @Test
    void fluxFromArray() {
        String[] names = new String[]{"One", "Two", "Three"};
        Flux<Object> objectFlux = Flux.fromArray(names);
        StepVerifier.create(objectFlux)
                .expectNext("One", "Two", "Three")
                .verifyComplete();
    }

    @Test
    void fluxFromArray2() {
        Object[] names = stringList.toArray();
        Flux<Object> objectFlux = Flux.fromArray(names);
        StepVerifier.create(objectFlux)
                .expectNext("One", "Two", "Three")
                .verifyComplete();
    }

    @Test
    void fluxFromStream() {
        Stream<String> stringStream = Stream.of("One", "Two", "Three");
        Flux<String> stringFlux = Flux.fromStream(stringStream);
        StepVerifier.create(stringFlux)
                .expectNext("One", "Two", "Three")
                .verifyComplete();
    }

    @Test
    void monoFromJustOrEmpty_Present() {
        Mono<String> mono = Mono.justOrEmpty("Hi");
        StepVerifier.create(mono.log())
                .expectNext("Hi")
                .verifyComplete();
    }

    @Test
    void monoFromJustOrEmpty_Null() {
        Mono<String> mono = Mono.justOrEmpty(null); //Mono.emtpy() - equivalent
        StepVerifier.create(mono.log())
                .verifyComplete();
    }

    @Test
    void monoEmpty() {
        Mono<String> mono = Mono.empty();
        StepVerifier.create(mono.log())
                .verifyComplete();
    }

    @Test
    void monoFromJustOrEmpty_Optional() {
        Mono<String> mono = Mono.justOrEmpty(Optional.of("Hi"));
        StepVerifier.create(mono.log())
                .expectNext("Hi")
                .verifyComplete();
    }

    @Test
    void monoFromJustOrEmpty_OptionalEmpty() {
        Mono<String> mono = Mono.justOrEmpty(Optional.empty());
        StepVerifier.create(mono.log())
                .verifyComplete();
    }

    @Test
    void monoFromJustOrEmpty_OptionalNullable() {
        Mono<String> mono = Mono.justOrEmpty(Optional.ofNullable(null));
        StepVerifier.create(mono.log())
                .verifyComplete();
    }

    @Test
    void monoUsingSupplier() {
        Supplier<String> stringSupplier = () -> "Hi from supplier";
        Mono<String> mono = Mono.fromSupplier(stringSupplier);

        System.out.println("------Supplier------");
        System.out.println(stringSupplier);
        System.out.println("---Supplier.get()---");
        System.out.println(stringSupplier.get());
        System.out.println("--------------------");

        StepVerifier.create(mono.log())
                .expectNext("Hi from supplier")
                .verifyComplete();
    }

    @Test
    void fluxRange() {
        Flux<String> stringFlux = Flux.range(101, 5)
                .map(i -> "flux" + i);
        StepVerifier.create(stringFlux.log())
                .expectNext("flux101")
                .expectNextCount(4)
                .verifyComplete();

    }
}
