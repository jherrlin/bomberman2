package se.jherrlin.bomberman.gateway

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import se.jherrlin.bomberman.models.Counter


@Component
class MyProducer(private val kafkaTemplate: KafkaTemplate<String, String>) {
    fun send(s: String) {
        println("Sending on topic s1, key: a, value: $s")
        kafkaTemplate.send("s1", "a", s)
    }
}