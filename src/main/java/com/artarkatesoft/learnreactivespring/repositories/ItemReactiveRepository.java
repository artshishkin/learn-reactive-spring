package com.artarkatesoft.learnreactivespring.repositories;

import com.artarkatesoft.learnreactivespring.documents.Item;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ItemReactiveRepository extends ReactiveMongoRepository<Item, String> {
    Flux<Item> findAllByDescription(String description);
}
