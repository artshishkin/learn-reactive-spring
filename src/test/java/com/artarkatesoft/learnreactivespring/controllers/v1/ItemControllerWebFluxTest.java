package com.artarkatesoft.learnreactivespring.controllers.v1;

import com.artarkatesoft.learnreactivespring.documents.Item;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveRepository;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.artarkatesoft.learnreactivespring.constants.ItemConstants.ITEM_END_POINT_V1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(controllers = ItemController.class)
class ItemControllerWebFluxTest {

    @MockBean
    ItemReactiveRepository itemRepository;

    @Autowired
    WebTestClient webTestClient;

    private Item defaultItem;
    private Flux<Item> repositoryFlux;
    private List<Item> itemsInRepo;

    @Captor
    ArgumentCaptor<Item> itemCaptor;

    @BeforeEach
    void setUp() {
        defaultItem = new Item("MyId", "desc4", 123.99);
        itemsInRepo = IntStream
                .rangeClosed(1, 5)
                .mapToObj(i -> new Item("id" + i, "desc" + i, (double) (i * 111)))
                .collect(Collectors.toList());
        itemsInRepo.add(defaultItem);
        repositoryFlux = Flux.fromIterable(itemsInRepo);
    }


    @Test
    void getAllItemsTest() {
        //given
        given(itemRepository.findAll()).willReturn(repositoryFlux);
        //when
        EntityExchangeResult<List<Item>> result = webTestClient.get().uri(ITEM_END_POINT_V1)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Item.class)
                .hasSize(6)
                .returnResult();
        //then
        then(itemRepository).should().findAll();
        assertThat(result.getResponseBody()).containsExactlyInAnyOrderElementsOf(itemsInRepo);
    }

    @Test
    void getOneItem_whenAbsent() {
        //given
        given(itemRepository.findById(anyString())).willReturn(Mono.empty());
        //when
        webTestClient.get().uri(ITEM_END_POINT_V1.concat("/idEmpty"))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .isEmpty();
        //then
        then(itemRepository).should().findById(eq("idEmpty"));
    }

    @Test
    void getOneItem_whenPresent() {
        //given
        given(itemRepository.findById(anyString())).willReturn(Mono.just(defaultItem));
        //when
        webTestClient.get().uri(ITEM_END_POINT_V1.concat("/MyId"))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Item.class)
                .isEqualTo(defaultItem);
        //then
        then(itemRepository).should().findById(eq("MyId"));
    }

    @Test
    void createItem() {
        //given
        String defaultId = defaultItem.getId();
        given(itemRepository.save(any(Item.class))).willReturn(Mono.just(defaultItem));
        //when
        webTestClient.post().uri(ITEM_END_POINT_V1)
                .bodyValue(defaultItem)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().value(HttpHeaders.LOCATION, CoreMatchers.endsWith(ITEM_END_POINT_V1 + "/" + defaultId))
                .expectBody(Item.class)
                .isEqualTo(defaultItem);
        //then
        then(itemRepository).should().save(itemCaptor.capture());
        Item itemToSave = itemCaptor.getValue();
        assertAll(
                () -> assertThat(defaultItem).isEqualToIgnoringNullFields(itemToSave),
                () -> assertThat(itemToSave.getId()).isNull()
        );
    }

    @Test
    void deleteItem_Present() {
        //given
        String defaultId = defaultItem.getId();
        given(itemRepository.findById(anyString())).willReturn(Mono.just(defaultItem));
        given(itemRepository.delete(any(Item.class))).willReturn(Mono.empty());
        //when
        webTestClient.delete().uri(ITEM_END_POINT_V1.concat("/" + defaultId))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        //then
        then(itemRepository).should().findById(eq(defaultItem.getId()));
        then(itemRepository).should().delete(eq(defaultItem));
    }

    @Test
    void deleteItem_Absent() {
        //given
        given(itemRepository.findById(anyString())).willReturn(Mono.empty());
        //when
        webTestClient.delete().uri(ITEM_END_POINT_V1.concat("/absentId"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .isEmpty();
        //then
        then(itemRepository).should().findById(eq("absentId"));
        then(itemRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("update EXISTING item")
    void updateItem_whenPresent() {
        //given
        String defaultId = defaultItem.getId();
        Item newItem = new Item("123", "New Description", 666.666);
        Item savedItem = new Item(defaultId, "New Description", 666.666);

        given(itemRepository.findById(anyString())).willReturn(Mono.just(defaultItem));
        given(itemRepository.save(any(Item.class))).willReturn(Mono.just(savedItem));

        //when
        webTestClient.put().uri(ITEM_END_POINT_V1 + "/{id}", defaultId)
                .bodyValue(newItem)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Item.class)
                .value(item -> assertAll(
                        () -> assertThat(item.getId()).isEqualTo(defaultItem.getId()),
                        () -> assertThat(item.getDescription()).isEqualTo(newItem.getDescription()),
                        () -> assertThat(item.getPrice()).isEqualTo(newItem.getPrice())
                ));
        //then
        then(itemRepository).should().findById(eq(defaultId));
        then(itemRepository).should().save(eq(savedItem));
    }

    @Test
    @DisplayName("update ABSENT item")
    void updateItem_whenAbsent() {
        //given
        Item newItem = new Item("123", "New Description", 666.666);
        given(itemRepository.findById(anyString())).willReturn(Mono.empty());
        String updateId = "absentId";

        //when
        webTestClient.put().uri(ITEM_END_POINT_V1 + "/{id}", updateId)
                .bodyValue(newItem)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class);
        //then
        then(itemRepository).should().findById(eq(updateId));
        then(itemRepository).shouldHaveNoMoreInteractions();
    }


}
