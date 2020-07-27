package com.artarkatesoft.learnreactivespring.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(controllers = FluxAndMonoController.class)
class FluxAndMonoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void testFluxEndpoint() {
        //when
        Flux<Integer> integerFlux = webTestClient.get().uri("/flux")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                //then
                .expectStatus().isOk()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(integerFlux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    void testFluxEndpoint_size() {
        //when
        webTestClient.get().uri("/flux")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Integer.class)
                .hasSize(4);
    }

    @Test
    void testFluxEndpoint_EntityExchangeResult() {
        //given
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        //when
        EntityExchangeResult<List<Integer>> result = webTestClient.get().uri("/flux")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Integer.class)
                .hasSize(4)
                .returnResult();
        assertThat(result.getResponseBody()).isEqualTo(list);
    }

    @Test
    void testFluxEndpoint_ConsumeWith() {
        //given
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        //when
        webTestClient.get().uri("/flux")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Integer.class)
                .hasSize(4)
                .consumeWith(
                        result -> assertThat(result.getResponseBody()).isEqualTo(list)
                );
    }

    @Test
    void testFluxStreamEndpoint() {
        //when
        Flux<Long> longFlux = webTestClient.get().uri("/fluxstream")
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_STREAM_JSON)
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier.create(longFlux)
                .expectSubscription()
                .expectNext(0L, 1L, 2L, 3L)
                .thenCancel()
                .verify();
    }


}
