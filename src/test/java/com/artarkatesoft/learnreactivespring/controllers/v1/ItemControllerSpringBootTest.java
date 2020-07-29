package com.artarkatesoft.learnreactivespring.controllers.v1;

import com.artarkatesoft.learnreactivespring.documents.Item;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveRepository;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.artarkatesoft.learnreactivespring.constants.ItemConstants.ITEM_END_POINT_V1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.MediaType.APPLICATION_JSON;

//@Disabled("too long")
@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ItemControllerSpringBootTest {

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
                .mapToObj(i -> new Item("id" + i, "desc" + i, (double) (i * 111)))
                .collect(Collectors.toList());
        itemsInRepo.add(defaultItem);

        itemRepository.deleteAll()
                .thenMany(Flux.fromIterable(itemsInRepo))
                .flatMap(itemRepository::save)
                .log("ItemControllerSpringBootTest setUp")
                .blockLast();
    }

    @Test
    void getAllItemsTest() {
        //when
        EntityExchangeResult<List<Item>> result = webTestClient.get().uri(ITEM_END_POINT_V1)
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
    void getOneItem_whenAbsent() {
        //when
        webTestClient.get().uri(ITEM_END_POINT_V1.concat("/idEmpty"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .isEmpty();
        //then

    }

    @Test
    void getOneItem_whenPresent() {
        //when
        webTestClient.get().uri(ITEM_END_POINT_V1.concat("/MyId"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Item.class)
                .isEqualTo(defaultItem);
        //then
    }

    @Test
    void createItem_value() {
        //given
        Item newItem = new Item(null, "Description New", 1234.4321);
        String defaultId = defaultItem.getId();
        //when
        webTestClient.post().uri(ITEM_END_POINT_V1)
                .bodyValue(newItem)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().value(HttpHeaders.LOCATION, CoreMatchers.containsString(ITEM_END_POINT_V1 + "/"))
                .expectBody(Item.class)
                .value(item -> assertAll(
                        () -> assertThat(item).isEqualToIgnoringNullFields(newItem),
                        () -> assertThat(item.getId()).isNotBlank()
                ));
    }

    @Test
    void createItem_jsonPath() {
        //given
        Item newItem = new Item(null, "Description New", 1234.4321);
        //when
        webTestClient.post().uri(ITEM_END_POINT_V1)
                .bodyValue(newItem)
                .exchange()
                //then
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().value(HttpHeaders.LOCATION, CoreMatchers.containsString(ITEM_END_POINT_V1 + "/"))
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.description").isEqualTo(newItem.getDescription())
                .jsonPath("$.price").isEqualTo(newItem.getPrice());
    }

    @Test
    @DisplayName("update EXISTING item")
    void updateItem_whenPresent() {
        //given
        String defaultId = defaultItem.getId();
        Item newItem = new Item("123", "New Description", 666.666);
        Item savedItem = new Item(defaultId, "New Description", 666.666);
        String updateId = defaultId;
        //when
        webTestClient.put().uri(ITEM_END_POINT_V1 + "/{id}", updateId)
                .bodyValue(newItem)
                .accept(APPLICATION_JSON)
                .exchange()
                //then
                .expectStatus().isOk()
                .expectBody(Item.class)
                .isEqualTo(savedItem);
    }

    @Test
    @DisplayName("update ABSENT item")
    void updateItem_whenAbsent() {
        //given
        String defaultId = defaultItem.getId();
        Item newItem = new Item("123", "New Description", 666.666);
        String updateId = "absentId";
        //when
        webTestClient.put().uri(ITEM_END_POINT_V1 + "/{id}", updateId)
                .bodyValue(newItem)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class);
    }


}
