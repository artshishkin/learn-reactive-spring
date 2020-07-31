package com.artarkatesoft.learnreactivespring.controllers.v1;

import com.artarkatesoft.learnreactivespring.documents.Item;
import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;

import static com.artarkatesoft.learnreactivespring.constants.ItemConstants.ITEM_END_POINT_V1;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping(ITEM_END_POINT_V1)
@Slf4j
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
    public Mono<ResponseEntity<Item>> createItem(@RequestBody Item item, ServerHttpRequest request, UriComponentsBuilder uriComponentsBuilder) {
        item.setId(null);
        return itemRepository
                .save(item)
                .map(itemSaved -> ResponseEntity
                        .created(uriComponentsBuilder.uri(request.getURI()).pathSegment(itemSaved.getId()).build().toUri())
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

    @GetMapping("runtimeExceptionOnly")
    public Mono<Void> runtimeExceptionOnly() {
        throw new RuntimeException("Runtime Exception occurred");
    }

    @GetMapping("illegalStateException")
    public Mono<Void> illegalStateException() {
        throw new IllegalStateException("Illegal State Exception occurred");
    }
    @GetMapping("arithmeticException")
    public Mono<Void> arithmeticException() {
        throw new ArithmeticException("Arithmetic Exception occurred");
    }

    @GetMapping("fileException")
    public Mono<Void> catchableException() throws FileNotFoundException {
        throw new FileNotFoundException("File Not Found Exception occurred");
    }

    @GetMapping("runtimeException")
    public Flux<Item> runtimeException() {
        return itemRepository.findAll()
                .log("From runtimeException")
                .concatWith(Mono.error(new RuntimeException("Runtime Exception occurred")));
    }

//    @ExceptionHandler
//    public Mono<ResponseEntity<String>> runtimeExceptionHandler(RuntimeException exception) {
//        return Mono.just(exception.getMessage())
//                .map(message -> ResponseEntity
//                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(message));
//    }

//    @ExceptionHandler(RuntimeException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public Mono<String> runtimeExceptionHandler(RuntimeException exception) {
//        log.error("Exception caught in runtimeExceptionHandler: ", exception);
//        return Mono.just(exception.getMessage());
//    }

    @ExceptionHandler(ArithmeticException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String arithmeticExceptionHandler(RuntimeException exception) {
        log.error("ArithmeticException caught in arithmeticExceptionHandler:", exception);
        return exception.getMessage();
    }

}
