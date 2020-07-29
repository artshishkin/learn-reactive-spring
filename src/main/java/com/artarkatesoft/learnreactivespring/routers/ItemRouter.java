package com.artarkatesoft.learnreactivespring.routers;

import com.artarkatesoft.learnreactivespring.handlers.ItemHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static com.artarkatesoft.learnreactivespring.constants.ItemConstants.ITEM_FUNCTIONAL_END_POINT_V1;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class ItemRouter {

    @Bean
    public RouterFunction<ServerResponse> itemRoute(ItemHandler handler) {
        return RouterFunctions
                .route(GET(ITEM_FUNCTIONAL_END_POINT_V1).and(accept(APPLICATION_JSON)),
                        handler::getAllItems)
                .andRoute(GET(ITEM_FUNCTIONAL_END_POINT_V1 + "/{id}").and(accept(APPLICATION_JSON)),
                        handler::getOneItem);
    }
}
