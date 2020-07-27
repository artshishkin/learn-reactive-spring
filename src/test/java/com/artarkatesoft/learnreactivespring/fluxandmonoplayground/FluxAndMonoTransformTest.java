package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static reactor.core.scheduler.Schedulers.parallel;

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

//flatMap------------------------------------------------------------------------------------

    @Test
    void fluxTransformUsingFlatMap() {
        Flux<Character> characterFlux = Flux.fromIterable(names)
                .flatMap((name) -> Flux.fromStream(fromString(name)));
        StepVerifier.create(characterFlux.log())
                .expectNext('a', 'r', 't', 'k', 'a', 't', 'e')
                .expectNextCount(10)
                .verifyComplete();
    }

    private Stream<Character> fromString(String str) {
        return str.chars().mapToObj(c -> (char) c);
    }

    @Test
    void fluxTransformUsingFlatMap2() {
        Flux<Character> characterFlux = Flux
                .fromIterable(names)
                .map(this::fromString)
                .flatMap(Flux::fromStream);

        StepVerifier.create(characterFlux.log())
                .expectNext('a', 'r', 't', 'k', 'a', 't', 'e')
                .expectNextCount(10)
                .verifyComplete();
    }

    @Test
    void fluxTransformUsingFlatMap3() {
        Flux<String> stringFlux = Flux
                .just("A", "B", "C")
                .flatMap(this::fakeDBCall);

        StepVerifier.create(stringFlux.log())
                .expectNext("artA", "kateA", "arinaA", "nazarA", "artB")
                .expectNextCount(7)
                .verifyComplete();
    }

    private Flux<String> fakeDBCall(String str) {
        return Flux.fromIterable(names).map(name -> name + str);
    }

    @Test
    void fluxTransformUsingFlatMap4() {
        Flux<String> stringFlux = Flux
                .just("A", "B", "C")
                .flatMap(a -> Flux.fromIterable(convertToList(a)));

        StepVerifier.create(stringFlux.log())
                .expectNextCount(6)
                .verifyComplete();
    }

    @Test
    void fluxTransformUsingFlatMap5() {
        Flux<String> stringFlux = Flux
                .just("A", "B", "C")
                .map(this::convertToList)
                .flatMap(Flux::fromIterable);

        StepVerifier.create(stringFlux.log())
                .expectNextCount(6)
                .verifyComplete();
    }

    private List<String> convertToList(String str) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Arrays.asList(str, "newVal" + str);
    }

    @Test
    @DisplayName("Execute in Parallel by pairs NOT ordered (threads: main, parallel-1, parallel-2, parallel-3)")
    void fluxTransformUsingFlatMap_usingParallel() {
        Flux<String> stringFlux = Flux
                .just("A", "B", "C", "D", "E", "F")
                .window(2) //Flux<Flux<String>> -> (A,B) (C,D) (E,F)
                .flatMap(flux -> flux
                        .map(this::convertToList)
                        .subscribeOn(parallel())
                        .flatMap(Flux::fromIterable))
                .log();

        StepVerifier.create(stringFlux.log())
                .expectNextCount(12)
                .verifyComplete();
    }

    @Test
    @DisplayName("Execute in Parallel by pairs ORDERED (threads: main, parallel-1)")
    void fluxTransformUsingFlatMap_usingParallel_andOrdered() {
        Flux<String> stringFlux = Flux
                .just("A", "B", "C", "D", "E", "F")
                .window(2) //Flux<Flux<String>> -> (A,B) (C,D) (E,F)
                .flatMapSequential(flux -> flux
                        .map(this::convertToList)
                        .subscribeOn(parallel())
                        .flatMap(Flux::fromIterable))
                .log();

        StepVerifier.create(stringFlux.log())
                .expectNextCount(12)
                .verifyComplete();
    }


}
