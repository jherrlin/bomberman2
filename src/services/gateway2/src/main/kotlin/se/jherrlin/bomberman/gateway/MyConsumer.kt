package se.jherrlin.bomberman.gateway

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class MyConsumer {
    @KafkaListener(topics = ["s1"])
    fun processMessage(content: String?) = println("MyConsumer says: $content")
}