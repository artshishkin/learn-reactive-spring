package com.artarkatesoft.learnreactivespring.handlers;

import com.artarkatesoft.learnreactivespring.documents.ItemCapped;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveCappedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;

@Component
@RequiredArgsConstructor
public class ItemCappedHandler {

    private final ItemReactiveCappedRepository itemCappedRepository;

    private static Mono<ServerResponse> notFound = ServerResponse.notFound().build();

    public Mono<ServerResponse> streamItems(ServerRequest request) {
        Flux<ItemCapped> itemCappedFlux = itemCappedRepository.findAllBy();
        return ServerResponse.ok().contentType(APPLICATION_STREAM_JSON).body(itemCappedFlux, ItemCapped.class);
    }
}
