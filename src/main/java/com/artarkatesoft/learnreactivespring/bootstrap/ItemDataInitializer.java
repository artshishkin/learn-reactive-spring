package com.artarkatesoft.learnreactivespring.bootstrap;

import com.artarkatesoft.learnreactivespring.documents.Item;
import com.artarkatesoft.learnreactivespring.documents.ItemCapped;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class ItemDataInitializer implements CommandLineRunner {

    private final ItemReactiveRepository repository;
    private final ReactiveMongoOperations mongoOperations;

    @Override
    public void run(String... args) throws Exception {
        bootstrapItemData();
        createCappedCollection();
    }

    private void createCappedCollection() {
        mongoOperations.dropCollection(ItemCapped.class);
        mongoOperations.createCollection(ItemCapped.class, CollectionOptions.empty()
                .maxDocuments(20).size(50000).capped());
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
