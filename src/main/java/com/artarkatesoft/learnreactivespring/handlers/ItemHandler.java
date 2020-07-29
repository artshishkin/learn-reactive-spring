package com.artarkatesoft.learnreactivespring.handlers;

import com.artarkatesoft.learnreactivespring.documents.Item;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
@RequiredArgsConstructor
public class ItemHandler {

    private final ItemReactiveRepository itemRepository;

    private static Mono<ServerResponse> notFound = ServerResponse.notFound().build();

    public Mono<ServerResponse> getAllItems(ServerRequest request) {
        return ServerResponse
                .ok()
                .contentType(APPLICATION_JSON)
                .body(itemRepository.findAll(), Item.class);
    }

    public Mono<ServerResponse> getOneItem(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Item> itemMono = itemRepository.findById(id);

        return itemMono.flatMap(item -> ServerResponse
                .ok()
                .contentType(APPLICATION_JSON)
                .body(fromValue(item))
        )
                .switchIfEmpty(notFound);
    }
}
