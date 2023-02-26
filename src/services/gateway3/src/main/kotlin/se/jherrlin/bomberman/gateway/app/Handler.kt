package se.jherrlin.bomberman.gateway.app

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import se.jherrlin.bomberman.gateway.streams.WordCountStream


@Component
class Handler(
    private val wordCountStream: WordCountStream
) {
    fun queryWordCountStreamStore(serverRequest: ServerRequest): Mono<ServerResponse> {
        val word = serverRequest.pathVariable("word")
        val wordsCount = wordCountStream.queryCountStreamStore(word)
        return ServerResponse.ok().body(BodyInserters.fromValue<Any>(wordsCount))
    }

    private fun notFound(): Mono<ServerResponse> {
        return ServerResponse.notFound().build()
    }
}