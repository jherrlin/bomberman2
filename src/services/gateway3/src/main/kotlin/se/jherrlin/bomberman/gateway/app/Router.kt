package se.jherrlin.bomberman.gateway.app


import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse


@Configuration
class Router {
    @Bean
    fun routes(handler: Handler): RouterFunction<ServerResponse> {
        return route<ServerResponse>(
            GET("/wordscount/{word}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
            handler::queryWordCountStreamStore
        ).andRoute(
            GET("/wordscountwindowed/{word}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
            handler::queryWordCountWindowedStreamStore
        )
    }
}