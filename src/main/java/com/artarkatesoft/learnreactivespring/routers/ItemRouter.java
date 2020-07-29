package com.artarkatesoft.learnreactivespring.routers;

import com.artarkatesoft.learnreactivespring.handlers.ItemHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ItemRouter {

    @Bean
    public RouterFunction<ServerResponse> itemRoute(ItemHandler handler){
        throw new RuntimeException("Not implemented yet");
    }
}
