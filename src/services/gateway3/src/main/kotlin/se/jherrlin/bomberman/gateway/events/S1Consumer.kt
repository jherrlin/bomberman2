package se.jherrlin.bomberman.gateway.events

import mu.KotlinLogging.logger
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class S1Consumer {
    val logger = logger {}
    @KafkaListener(topics = ["s1"])
    fun processMessage(content: String?) = logger.debug { "s1 topic got event: $content" }
}