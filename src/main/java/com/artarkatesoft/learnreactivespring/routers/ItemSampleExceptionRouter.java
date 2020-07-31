package com.artarkatesoft.learnreactivespring.routers;

import com.artarkatesoft.learnreactivespring.handlers.ItemSampleExceptionsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class ItemSampleExceptionRouter {

    @Bean
    public RouterFunction<ServerResponse> itemSampleExceptionRoute(ItemSampleExceptionsHandler handler) {
        return RouterFunctions.route(GET("/fun/runtimeException"),
                handler::runtimeException);
    }
}
