package com.artarkatesoft.learnreactivespring.controllers.v1;

import com.artarkatesoft.learnreactivespring.documents.ItemCapped;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveCappedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.artarkatesoft.learnreactivespring.constants.ItemConstants.ITEM_STREAM_END_POINT_V1;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@WebFluxTest(controllers = ItemStreamController.class)
class ItemStreamControllerWebFluxTest {

    @MockBean
    ItemReactiveCappedRepository repository;

    @Autowired
    WebTestClient webTestClient;
    private Flux<ItemCapped> itemCappedFlux;

    @BeforeEach
    void setUp() {
        itemCappedFlux = Flux.range(1, 5)
                .delayElements(Duration.ofMillis(10))
                .map(i -> new ItemCapped("id" + i, "Item Capped Description" + i, 100.0 + 1.1 * i))
                .log("generate itemCapped").take(5);
    }

    @Test
    void streamItem() {
        //given
        given(repository.findAllBy()).willReturn(itemCappedFlux);
        //when
        Flux<ItemCapped> itemCappedFluxReceived = webTestClient.get().uri(ITEM_STREAM_END_POINT_V1)
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_STREAM_JSON)
                .returnResult(ItemCapped.class)
                .getResponseBody();
        StepVerifier.create(itemCappedFluxReceived)
                .expectSubscription()
//                .thenAwait(Duration.ofSeconds(1))
                .expectNextCount(5)
                .thenCancel()
                .verify();
        then(repository).should().findAllBy();
        then(repository).shouldHaveNoMoreInteractions();
    }
}
