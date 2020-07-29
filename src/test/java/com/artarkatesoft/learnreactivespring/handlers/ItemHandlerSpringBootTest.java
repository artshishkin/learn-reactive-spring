package com.artarkatesoft.learnreactivespring.handlers;

import com.artarkatesoft.learnreactivespring.documents.Item;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.artarkatesoft.learnreactivespring.constants.ItemConstants.ITEM_FUNCTIONAL_END_POINT_V1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ItemHandlerSpringBootTest {
    @Autowired
    ItemReactiveRepository itemRepository;

    @Autowired
    WebTestClient webTestClient;

    private Item defaultItem;
    private List<Item> itemsInRepo;

    @BeforeEach
    void setUp() {
        defaultItem = new Item("MyId", "desc4", 123.99);
        itemsInRepo = IntStream
                .rangeClosed(1, 5)
                .mapToObj(i -> new Item("id" + i, "desc" + i, i * 111.11))
                .collect(Collectors.toList());
        itemsInRepo.add(defaultItem);

        itemRepository.deleteAll()
                .thenMany(Flux.fromIterable(itemsInRepo))
                .flatMap(itemRepository::save)
                .log("ItemHandlerSpringBootTest setUp")
                .blockLast();
    }

    @Test
    @DisplayName("when accessing GET ALL Endpoint should return All items from DB")
    void getAllItemsTest() {
        //when
        EntityExchangeResult<List<Item>> result = webTestClient.get().uri(ITEM_FUNCTIONAL_END_POINT_V1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Item.class)
                .hasSize(6)
                .returnResult();
        //then
        assertThat(result.getResponseBody()).containsExactlyInAnyOrderElementsOf(itemsInRepo);
    }

    @Test
    @DisplayName("assert that there is NO NULL ID values when getting ALL items")
    void getAllItemsTest_idNotNull() {

        //when
        webTestClient
                .get()
                .uri(ITEM_FUNCTIONAL_END_POINT_V1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBodyList(Item.class)
                .hasSize(6)
                .value(itemList ->
                        assertThat(itemList)
                                .allSatisfy(item -> assertThat(item.getId()).isNotBlank()));
    }

    @Test
    @DisplayName("by using Flux assert that there is NO NULL ID values when getting ALL items")
    void getAllItemsTest_idNotNull_usingFlux() {

        //when
        Flux<Item> itemFlux = webTestClient
                .get()
                .uri(ITEM_FUNCTIONAL_END_POINT_V1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()

                //then
                .expectStatus().isOk()
                .returnResult(Item.class)
                .getResponseBody();

        StepVerifier.create(itemFlux)
                .thenConsumeWhile(
                        item -> true,
                        item -> assertNotNull(item.getId())
                )
                .verifyComplete();
    }


}
