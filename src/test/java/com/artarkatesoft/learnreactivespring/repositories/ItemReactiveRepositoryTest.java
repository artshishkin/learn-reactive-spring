package com.artarkatesoft.learnreactivespring.repositories;

import com.artarkatesoft.learnreactivespring.documents.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataMongoTest
class ItemReactiveRepositoryTest {

    @Autowired
    ItemReactiveRepository repository;
    private Item defaultItem;

    @BeforeEach
    void setUp() {

        List<Item> items = IntStream
                .rangeClosed(1, 5)
                .mapToObj(i -> new Item(null, "desc" + i, (double) (i * 111)))
                .collect(Collectors.toList());
        defaultItem = new Item("MyId", "desc4", 123.99);
        Flux<Item> initFlux = repository.deleteAll()
                .thenMany(Flux.fromIterable(items).concatWith(Flux.just(defaultItem)))
                .flatMap(repository::save)
                .doOnNext(item -> System.out.println("Inserted Item: " + item));
        initFlux.blockLast(); //Use this ONLY in test cases
    }

    @Test
    void getAllItems() {
        Flux<Item> itemFlux = repository.findAll();

        StepVerifier.create(itemFlux)
                .expectSubscription()
                .expectNextCount(6L)
                .verifyComplete();
    }

    @Test
    void getItemById() {
        String defaultId = defaultItem.getId();
        Mono<Item> itemMono = repository.findById(defaultId);
        StepVerifier.create(itemMono)
                .expectSubscription()
                .expectNext(defaultItem)
                .verifyComplete();
    }

    @Test
    void getItemById_NextMatches() {
        String defaultId = defaultItem.getId();
        StepVerifier.create(repository.findById(defaultId))
                .expectSubscription()
                .expectNextMatches(item -> item.getDescription().equals(defaultItem.getDescription()))
                .verifyComplete();
    }

    @Test
    void getItemById_Absent() {
        String absentId = "Absent ID";
        Mono<Item> itemMono = repository.findById(absentId);
        StepVerifier.create(itemMono)
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getItemsByDescription_oneResult() {
        //given
        String description = "desc2";
        //when
        Flux<Item> itemFlux = repository.findAllByDescription(description);
        //then
        StepVerifier.create(itemFlux)
                .expectSubscription()
                .expectNextMatches(item -> item.getDescription().equals(description))
                .verifyComplete();
    }

    @Test
    void getItemsByDescription_twoResults() {
        //given
        String description = "desc4";
        //when
        Flux<Item> itemFlux = repository.findAllByDescription(description).log("getItemsByDescription_twoResults");
        //then
        StepVerifier.create(itemFlux)
                .expectSubscription()
                .expectNextMatches(item -> item.getDescription().equals(description))
                .expectNextMatches(item -> item.getDescription().equals(description))
                .verifyComplete();
    }

    @Test
    void getItemsByDescription_noResults() {
        //given
        String description = "Not Found Description";
        //when
        Flux<Item> itemFlux = repository.findAllByDescription(description);
        //then
        StepVerifier.create(itemFlux)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void insertItemTest() {
        //given
        Item itemToInsert = new Item(null, "Insert description", 123.45);
        //when
        Mono<Item> savedItemMono = repository.save(itemToInsert).log("insertItemTest");
        //then
        StepVerifier.create(savedItemMono)
                .expectSubscription()
                .assertNext(item ->
                        assertAll(
                                () -> assertThat(item).isEqualToIgnoringNullFields(itemToInsert),
                                () -> assertThat(item.getId()).isNotBlank()
                        ))
                .verifyComplete();
    }
}
