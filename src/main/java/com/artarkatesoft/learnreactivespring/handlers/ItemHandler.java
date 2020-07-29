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

    public Mono<ServerResponse> createItem(ServerRequest request) {

        Mono<Item> itemMono = request.bodyToMono(Item.class);
        Mono<Item> savedItemMono = itemMono
                .flatMap(item -> {
                    item.setId(null);
                    return itemRepository.save(item);
                });
        return savedItemMono
                .flatMap(itemSaved -> ServerResponse.created(request.uriBuilder().pathSegment(itemSaved.getId()).build())
                        .contentType(APPLICATION_JSON)
                        .body(fromValue(itemSaved)));
    }

    public Mono<ServerResponse> deleteItem(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Item> itemMono = itemRepository.findById(id);
        return itemMono.flatMap(item -> itemRepository.delete(item)
                .then(ServerResponse.ok().contentType(APPLICATION_JSON).build()))
                .switchIfEmpty(notFound);
    }
}
