package se.jherrlin.bomberman.gateway.app

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import se.jherrlin.bomberman.gateway.streams.WordCount
import se.jherrlin.bomberman.gateway.streams.WordCountWindowed


@Component
class Handler(
    private val wordCount: WordCount,
    private val wordCountWindowed: WordCountWindowed
) {
    fun queryWordCountStreamStore(serverRequest: ServerRequest): Mono<ServerResponse> {
        val word = serverRequest.pathVariable("word")
        val wordsCount = wordCount.queryWordCountStore(word)
        return ServerResponse.ok().body(BodyInserters.fromValue<Any>(wordsCount))
    }

    fun queryWordCountWindowedStreamStore(serverRequest: ServerRequest): Mono<ServerResponse> {
        val word = serverRequest.pathVariable("word")
        val wordsCount = wordCountWindowed.queryWordCountWindowedStore(word)
        return ServerResponse.ok().body(BodyInserters.fromValue<Any>(wordsCount))
    }

    private fun notFound(): Mono<ServerResponse> {
        return ServerResponse.notFound().build()
    }
}