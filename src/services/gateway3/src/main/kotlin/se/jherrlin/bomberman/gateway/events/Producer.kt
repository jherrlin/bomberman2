package se.jherrlin.bomberman.gateway.events

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component


@Component
class Producer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    fun send(topic: String, key: String, value: String) {
        println("Sending event on topic: $topic, key: $key, value: $value")
        kafkaTemplate.send(topic, key, value)
    }
}