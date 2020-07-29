package com.artarkatesoft.learnreactivespring.handlers;

import com.artarkatesoft.learnreactivespring.documents.Item;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveRepository;
import com.artarkatesoft.learnreactivespring.routers.ItemRouter;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.artarkatesoft.learnreactivespring.constants.ItemConstants.ITEM_FUNCTIONAL_END_POINT_V1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ItemHandlerMockTest {

    @Mock
    ItemReactiveRepository itemRepository;

    @InjectMocks
    ItemHandler itemHandler;

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

        RouterFunction<?> itemRouterFunction = new ItemRouter().itemRoute(itemHandler);
        webTestClient = WebTestClient.bindToRouterFunction(itemRouterFunction).build();
    }

    @Test
    @DisplayName("when accessing GET ALL Endpoint should return All items from DB")
    void getAllItemsTest() {
        //given
        given(itemRepository.findAll()).willReturn(repositoryFlux);
        //when
        EntityExchangeResult<List<Item>> result = webTestClient.get().uri(ITEM_FUNCTIONAL_END_POINT_V1)
                .accept(MediaType.APPLICATION_JSON)
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
    @DisplayName("assert that there is NO NULL ID values when getting ALL items")
    void getAllItemsTest_idNotNull() {
        //given
        given(itemRepository.findAll()).willReturn(repositoryFlux);
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
        then(itemRepository).should().findAll();
        then(itemRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("by using Flux assert that there is NO NULL ID values when getting ALL items")
    void getAllItemsTest_idNotNull_usingFlux() {
        //given
        given(itemRepository.findAll()).willReturn(repositoryFlux);
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
        then(itemRepository).should().findAll();
        then(itemRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void getOneItem_whenAbsent() {
        //given
        given(itemRepository.findById(anyString())).willReturn(Mono.empty());
        //when
        webTestClient.get().uri(ITEM_FUNCTIONAL_END_POINT_V1.concat("/idEmpty"))
                .accept(MediaType.APPLICATION_JSON)
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
        webTestClient.get().uri(ITEM_FUNCTIONAL_END_POINT_V1.concat("/MyId"))
                .accept(MediaType.APPLICATION_JSON)
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
        webTestClient.post().uri(ITEM_FUNCTIONAL_END_POINT_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(defaultItem)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().value(HttpHeaders.LOCATION, CoreMatchers.endsWith(ITEM_FUNCTIONAL_END_POINT_V1 + "/" + defaultId))
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
}
