package com.artarkatesoft.learnreactivespring.repositories;

import com.artarkatesoft.learnreactivespring.documents.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataMongoTest
class ItemReactiveRepositoryTest {

    @Autowired
    ItemReactiveRepository repository;
    private Item defaultItem;

    @BeforeEach
    void setUp() {

        Stream<Item> itemStream = IntStream
                .rangeClosed(1, 5)
                .mapToObj(i -> new Item(null, "desc" + i, (double) (i * 111)));
        defaultItem = new Item("MyId", "desc4", 123.99);
        Flux<Item> initFlux = repository.deleteAll()
                .thenMany(Flux.fromStream(itemStream).concatWith(Flux.just(defaultItem)))
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

    @Test
    void updateItemTest() {
        //given
        String defaultItemId = defaultItem.getId();
        Item modifiedItem = new Item(defaultItemId, "Modified Description", 121212.12);
        //when
        Mono<Item> savedItemMono = repository.save(modifiedItem).log("updateItemTest");
        //then
        StepVerifier.create(savedItemMono)
                .expectSubscription()
                .assertNext(item -> assertThat(item).isEqualTo(modifiedItem))
                .verifyComplete();
    }

    @Test
    void updateItemByDescription() {
        //given
        double newPrice = 54321.12;
        String descriptionToUpdate = "desc1";
        //when
        Flux<Item> itemFlux = repository.findAllByDescription(descriptionToUpdate)
                .map(item -> {
                    item.setPrice(newPrice);
                    return item;
                })
                .flatMap(repository::save);
        //then
        StepVerifier.create(itemFlux)
                .expectSubscription()
                .expectNextMatches(item -> item.getDescription().equals(descriptionToUpdate) &&
                        item.getPrice() == newPrice)
                .verifyComplete();
    }

    @Test
    void deleteItemById() {
        //given
        String defaultItemId = defaultItem.getId();
        //when
        Mono<Void> deletedItem = repository.deleteById(defaultItemId);
        //then
        StepVerifier.create(deletedItem.log())
                .expectSubscription()
                .verifyComplete();
        StepVerifier.create(repository.count())
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void deleteItemsByDescription() {
        //given
        String descriptionToDelete = "desc4";
        //when
        Flux<Void> itemFlux = repository.findAllByDescription(descriptionToDelete)
                .flatMap(repository::delete);
        //then
        StepVerifier.create(itemFlux.log("deleteItemsByDescription"))
                .expectSubscription()
                .verifyComplete();
        StepVerifier.create(repository.count())
                .expectNext(4L)
                .verifyComplete();
        StepVerifier
                .create(
                        repository
                                .findAll()
                                .map(Item::getDescription)
                                .filter(descriptionToDelete::equals))
                .expectSubscription()
                .verifyComplete();
    }
}
