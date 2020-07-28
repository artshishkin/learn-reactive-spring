package com.artarkatesoft.learnreactivespring.controllers.v1;

import com.artarkatesoft.learnreactivespring.documents.Item;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
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

@ExtendWith(MockitoExtension.class)
class ItemControllerMockTest {

    @Mock
    ItemReactiveRepository itemRepository;
    @InjectMocks
    ItemController itemController;

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

        webTestClient = WebTestClient.bindToController(itemController).build();
    }

    @Test
    void getAllItemsTest() {
        //given
        given(itemRepository.findAll()).willReturn(repositoryFlux);
        //when
        EntityExchangeResult<List<Item>> result = webTestClient.get().uri(ITEM_END_POINT_V1)
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
    void getOneItem_whenAbsent() {
        //given
        given(itemRepository.findById(anyString())).willReturn(Mono.empty());
        //when
        webTestClient.get().uri(ITEM_END_POINT_V1.concat("/idEmpty"))
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
        webTestClient.get().uri(ITEM_END_POINT_V1.concat("/MyId"))
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
        given(itemRepository.save(any(Item.class))).willReturn(Mono.just(defaultItem));
        //when
        webTestClient.post().uri(ITEM_END_POINT_V1)
                .bodyValue(defaultItem)
                .exchange()
                .expectStatus().isCreated()
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
