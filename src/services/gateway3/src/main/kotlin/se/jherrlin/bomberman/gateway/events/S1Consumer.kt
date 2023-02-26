package se.jherrlin.bomberman.gateway.events

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class S1Consumer {
    @KafkaListener(topics = ["s1"])
    fun processMessage(content: String?) = println("s1 topic got event: $content")
}