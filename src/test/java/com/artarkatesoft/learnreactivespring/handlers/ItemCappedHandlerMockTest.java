package com.artarkatesoft.learnreactivespring.handlers;

import com.artarkatesoft.learnreactivespring.documents.ItemCapped;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveCappedRepository;
import com.artarkatesoft.learnreactivespring.routers.ItemRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.artarkatesoft.learnreactivespring.constants.ItemConstants.ITEM_STREAM_FUNCTIONAL_END_POINT_V1;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;

@ExtendWith(MockitoExtension.class)
class ItemCappedHandlerMockTest {

    @Mock
    ItemReactiveCappedRepository repository;

    WebTestClient webTestClient;

    @InjectMocks
    ItemCappedHandler itemCappedHandler;

    private Flux<ItemCapped> itemCappedFlux;

    @BeforeEach
    void setUp() {
        itemCappedFlux = Flux.range(1, 5)
                .delayElements(Duration.ofMillis(10))
                .map(i -> new ItemCapped("id" + i, "Item Capped Description" + i, 100.0 + 1.1 * i))
                .log("generate itemCapped").take(5);

        RouterFunction<ServerResponse> routerFunction = new ItemRouter().itemStreamRoute(itemCappedHandler);
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    void streamItems() {
        //given
        given(repository.findAllBy()).willReturn(itemCappedFlux);
        //when
        Flux<ItemCapped> itemCappedFluxReceived = webTestClient.get().uri(ITEM_STREAM_FUNCTIONAL_END_POINT_V1)
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_STREAM_JSON)
                .returnResult(ItemCapped.class)
                .getResponseBody();
        StepVerifier.create(itemCappedFluxReceived)
                .expectSubscription()
                .expectNextCount(5)
                .thenCancel()
                .verify();
        then(repository).should().findAllBy();
        then(repository).shouldHaveNoMoreInteractions();
    }

}
