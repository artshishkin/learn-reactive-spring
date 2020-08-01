package com.artarkatesoft.learnreactivespring.controllers.v1;

import com.artarkatesoft.learnreactivespring.documents.ItemCapped;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveCappedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static com.artarkatesoft.learnreactivespring.constants.ItemConstants.ITEM_STREAM_END_POINT_V1;
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON_VALUE;

@RestController
@RequestMapping(ITEM_STREAM_END_POINT_V1)
@RequiredArgsConstructor
public class ItemStreamController {

    private final ItemReactiveCappedRepository repository;

    @GetMapping(produces = APPLICATION_STREAM_JSON_VALUE)
    public Flux<ItemCapped> streamItem() {
        return repository.findAllBy();
    }
}
