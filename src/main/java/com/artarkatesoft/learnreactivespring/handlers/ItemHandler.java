package com.artarkatesoft.learnreactivespring.handlers;

import com.artarkatesoft.learnreactivespring.repositories.ItemReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemHandler {

    private final ItemReactiveRepository itemRepository;

}
