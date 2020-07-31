package com.artarkatesoft.learnreactivespring.handlers;

import com.artarkatesoft.learnreactivespring.exceptions.FunctionalWebExceptionHandler;
import com.artarkatesoft.learnreactivespring.routers.ItemSampleExceptionRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;

@Disabled("Do not know YET how to run test without loading all application context")
@ExtendWith(MockitoExtension.class)
class ItemSampleExceptionsHandlerMockTest {

    WebTestClient webTestClient;

    @Mock
    ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        ItemSampleExceptionsHandler handler = new ItemSampleExceptionsHandler();
        RouterFunction<ServerResponse> routerFunction = new ItemSampleExceptionRouter()
                .itemSampleExceptionRoute(handler);
        HandlerStrategies handlerStrategies = HandlerStrategies
                .builder()
                .exceptionHandler(new FunctionalWebExceptionHandler(
                        new DefaultErrorAttributes(),
                        applicationContext,
                        new DefaultServerCodecConfigurer()))
                .build();

        webTestClient = WebTestClient
                .bindToRouterFunction(routerFunction)
                .handlerStrategies(handlerStrategies)
                .build();
    }

    @Test
    void runtimeException_json() {
        webTestClient.get().uri(ItemSampleExceptionRouter.ENDPOINT_FUN_RUNTIME_EXCEPTION)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo(ItemSampleExceptionsHandler.RUNTIME_EXCEPTION_MESSAGE);
    }

    @Test
    void runtimeException_text() {
        webTestClient.get().uri(ItemSampleExceptionRouter.ENDPOINT_FUN_RUNTIME_EXCEPTION)
                .accept(TEXT_PLAIN)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo(ItemSampleExceptionsHandler.RUNTIME_EXCEPTION_MESSAGE);
    }
}
