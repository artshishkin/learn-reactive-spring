package com.artarkatesoft.learnreactivespring.controllers.v1;

import com.artarkatesoft.learnreactivespring.documents.Item;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.artarkatesoft.learnreactivespring.constants.ItemConstants.ITEM_END_POINT_V1;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping(ITEM_END_POINT_V1)
public class ItemController {

    private final ItemReactiveRepository itemRepository;

    @GetMapping
    public Flux<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Item>> getOneItem(@PathVariable("id") String id) {
        return itemRepository
                .findById(id)
                .map(item -> new ResponseEntity<>(item, OK))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<Item>> createItem(@RequestBody Item item, UriComponentsBuilder uriComponentsBuilder) {
        item.setId(null);
        return itemRepository
                .save(item)
                .map(itemSaved -> ResponseEntity
                        .created(uriComponentsBuilder.path(ITEM_END_POINT_V1 + "/{id}").buildAndExpand(itemSaved.getId()).toUri())
                        .body(itemSaved));
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> deleteItem(@PathVariable String id) {
        return itemRepository.findById(id)
                .flatMap(item -> itemRepository.delete(item).then(Mono.just(new ResponseEntity<Void>(OK))))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

//    @DeleteMapping("{id}")
//    public Mono<Void> deleteItem(@PathVariable String id) {
//        return itemRepository.deleteById(id);
//
//    }

    //request: path variable - id, request body - item
    //retrieve item by id from DB
    //update item by values from request
    //save item
    //return item to the client
    @PutMapping("{id}")
    public Mono<ResponseEntity<Item>> updateItem(@PathVariable String id, @RequestBody Item newItem) {
        Mono<Item> itemMono = itemRepository.findById(id);

        return itemMono
                .map(
                        item -> {
                            BeanUtils.copyProperties(newItem, item);
                            item.setId(id);
                            return item;
                        })
                .flatMap(itemRepository::save)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }

}
