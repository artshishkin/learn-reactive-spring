package com.artarkatesoft.learnreactivespring.bootstrap;

import com.artarkatesoft.learnreactivespring.documents.Item;
import com.artarkatesoft.learnreactivespring.documents.ItemCapped;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveCappedRepository;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class ItemDataInitializer implements CommandLineRunner {

    private final ItemReactiveRepository repository;
    private final ReactiveMongoOperations mongoOperations;
    private final ItemReactiveCappedRepository itemCappedRepository;

    @Override
    public void run(String... args) throws Exception {
        bootstrapItemData();
        Mono<Void> collectionCreated = createCappedCollection();
        dataSetUpForCappedCollection(collectionCreated);
    }

    private Mono<Void> createCappedCollection() {
        return mongoOperations.dropCollection(ItemCapped.class)
                .then(mongoOperations
                        .createCollection(ItemCapped.class, CollectionOptions.empty()
                                .maxDocuments(20).size(50000).capped()).log("createCappedCollection")
                )
                .then(Mono.empty());
    }

    private void dataSetUpForCappedCollection(Mono<Void> collectionCreated) {
        collectionCreated
                .thenMany(Flux.interval(Duration.ofSeconds(1)))
                .map(i -> new ItemCapped(null, "Item Capped Description" + i, 100.0 + 1.1 * i))
                .flatMap(itemCappedRepository::insert)
                .subscribe(item -> log.info("Inserted item is {}", item));

    }

    private void bootstrapItemData() {
        Stream<Item> itemStream = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> new Item(null, "desc" + i, 111.11 * i));
        Item defaultItem = new Item(UUID.randomUUID().toString(), "Default Description", 999.99);

        repository.deleteAll()
                .thenMany(Flux.fromStream(itemStream).concatWith(Flux.just(defaultItem)))
                .flatMap(repository::save)
                .log("bootstrapItemData")
                .subscribe();
    }
}
