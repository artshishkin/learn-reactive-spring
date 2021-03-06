package com.artarkatesoft.learnreactivespring.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class FluxAndMonoController {
    @GetMapping("flux")
    public Flux<Integer> returnFlux() {
        return Flux
                .just(1, 2, 3, 4)
                .delayElements(Duration.ofMillis(500))
                .log();
    }

    @GetMapping(value = "fluxstream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Long> returnFluxStream() {
        return Flux
                .interval(Duration.ofMillis(500))
                .log();
    }

    @GetMapping("mono")
    public Mono<Integer> getMono() {
        return Mono.just(123).log();
    }
}
