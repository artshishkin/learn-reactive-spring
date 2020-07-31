package com.artarkatesoft.learnreactivespring.handlers;

import com.artarkatesoft.learnreactivespring.routers.ItemSampleExceptionRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.MediaType.*;

@SpringBootTest
@AutoConfigureWebTestClient
class ItemSampleExceptionsHandlerSpringBootTest {

    @Autowired
    WebTestClient webTestClient;


    @Test
    void runtimeException_json() {
        webTestClient.get().uri(ItemSampleExceptionRouter.ENDPOINT_FUN_RUNTIME_EXCEPTION)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectBody(String.class)
                .isEqualTo(ItemSampleExceptionsHandler.RUNTIME_EXCEPTION_MESSAGE);
    }

    @Test
    void runtimeException_text() {
        webTestClient.get().uri(ItemSampleExceptionRouter.ENDPOINT_FUN_RUNTIME_EXCEPTION)
                .accept(TEXT_PLAIN)
                .exchange()
                .expectBody(String.class)
                .isEqualTo(ItemSampleExceptionsHandler.RUNTIME_EXCEPTION_MESSAGE);
    }

    @Test
    void runtimeException_html() {
        webTestClient.get().uri(ItemSampleExceptionRouter.ENDPOINT_FUN_RUNTIME_EXCEPTION)
                .accept(TEXT_HTML)
                .exchange()
                .expectBody(String.class)
                .isEqualTo(ItemSampleExceptionsHandler.RUNTIME_EXCEPTION_MESSAGE);
    }
}
