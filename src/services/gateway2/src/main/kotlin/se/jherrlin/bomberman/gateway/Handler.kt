package se.jherrlin.bomberman.gateway


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import se.jherrlin.bomberman.models.Course


@Component
class Handler @Autowired constructor(
    val myProducer: MyProducer,
    val streamCounterController: StreamCounterController
) {

    fun sendToS1(serverRequest: ServerRequest): Mono<ServerResponse> {
        val s = serverRequest.pathVariable("s")
        myProducer.send(s)
        return ServerResponse.ok().build()
    }

    fun findAllCourses(serverRequest: ServerRequest?): Mono<ServerResponse> {
        val courses: Flux<Course> = Flux.fromIterable(listOf(
            Course("1", "Course"),
            Course("2", "Course 2")
        ))
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(courses, Course::class.java)
    }

    fun findCourseById(serverRequest: ServerRequest): Mono<ServerResponse> {
        val courseId = serverRequest.pathVariable("id")

        val courses = listOf(
            Course("1", "Course"),
            Course("2", "Course 2")
        )

        val courseMono: Mono<Course> = courses.find { it.id == courseId }.let { Mono.just(it!!) }

        return courseMono.flatMap { course: Course ->
            ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue<Any>(course)) }
            .switchIfEmpty(notFound())
    }

    fun getFromStore(serverRequest: ServerRequest): Mono<ServerResponse> {
        val key = serverRequest.pathVariable("key")
        val v = streamCounterController.queryStore(key)
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue<Any>(v))
    }

    private fun notFound(): Mono<ServerResponse> {
        return ServerResponse.notFound().build()
    }
}
