package se.jherrlin.bomberman.gateway


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse


@Configuration
class RouterContext {
    @Bean
    fun routes(handler: Handler): RouterFunction<ServerResponse> {
        return route<ServerResponse>(
            GET("/courses").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
            handler::findAllCourses
        )
            .andRoute(
                GET("/courses/{id}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                handler::findCourseById
            )
            .andRoute(
                GET("/send/{s}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                handler::sendToS1
            )
            .andRoute(
                GET("/get/{s}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                handler::getFromStore
            )
    }
}
