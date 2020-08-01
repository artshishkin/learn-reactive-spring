package com.artarkatesoft.learnreactivespring.handlers;

import com.artarkatesoft.learnreactivespring.constants.ItemConstants;
import com.artarkatesoft.learnreactivespring.documents.ItemCapped;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveCappedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ItemCappedHandlerSpringBootTest {

    @Autowired
    ItemReactiveCappedRepository itemCappedRepository;

    @Autowired
    ReactiveMongoOperations mongoOperations;

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        mongoOperations.dropCollection(ItemCapped.class)
                .then(mongoOperations
                        .createCollection(
                                ItemCapped.class,
                                CollectionOptions.empty().maxDocuments(20).size(50000).capped()
                        )
                )
                .thenMany(
                        Flux.interval(Duration.ofMillis(100))
                ).map(i -> new ItemCapped(null, "Item Capped Description" + i, 100.0 + 1.1 * i))
                .log("generate itemCapped")
                .take(5L)
                .flatMap(itemCappedRepository::insert)
                .log("insert itemCapped")
                .blockLast();
    }

    @Test
    void streamItem() {
        //when
        Flux<ItemCapped> itemCappedFlux = webTestClient.get().uri(ItemConstants.ITEM_STREAM_FUNCTIONAL_END_POINT_V1)
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_STREAM_JSON)
                .returnResult(ItemCapped.class)
                .getResponseBody();
        StepVerifier.create(itemCappedFlux)
                .expectNextCount(5)
                .thenCancel()
                .verify();
    }
}
