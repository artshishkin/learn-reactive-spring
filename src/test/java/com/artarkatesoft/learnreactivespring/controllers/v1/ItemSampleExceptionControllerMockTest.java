package com.artarkatesoft.learnreactivespring.controllers.v1;

import com.artarkatesoft.learnreactivespring.constants.ItemConstants;
import com.artarkatesoft.learnreactivespring.exceptions.ControllerExceptionHandlers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.artarkatesoft.learnreactivespring.controllers.v1.ItemSampleExceptionController.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class ItemSampleExceptionControllerMockTest {

    private static WebTestClient webTestClient;

    @BeforeAll
    static void beforeAllSetUp() {
        webTestClient = WebTestClient
                .bindToController(new ItemSampleExceptionController())
                .controllerAdvice(new ControllerExceptionHandlers())
                .build();

    }

    @Test
    void illegalStateException() {
        webTestClient.get().uri(ItemConstants.ITEM_END_POINT_V1+"/illegalStateException")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo(ILLEGAL_STATE_EXCEPTION_MESSAGE);
    }

    @Test
    void arithmeticException() {
        webTestClient.get().uri(ItemConstants.ITEM_END_POINT_V1+"/arithmeticException")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo(ARITHMETIC_EXCEPTION_MESSAGE);
    }

    @Test
    void catchableException() {
        webTestClient.get().uri(ItemConstants.ITEM_END_POINT_V1+"/fileException")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo(FILE_NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @Test
    void runtimeException() {
        webTestClient.get().uri(ItemConstants.ITEM_END_POINT_V1+"/runtimeException")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo(RUNTIME_EXCEPTION_MESSAGE);
    }
}