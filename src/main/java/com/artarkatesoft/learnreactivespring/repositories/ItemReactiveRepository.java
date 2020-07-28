package com.artarkatesoft.learnreactivespring.repositories;

import com.artarkatesoft.learnreactivespring.documents.Item;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ItemReactiveRepository extends ReactiveMongoRepository<Item, String> {
}
