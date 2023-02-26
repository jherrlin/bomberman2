package se.jherrlin.bomberman.gateway

import org.springframework.context.annotation.Bean
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.stereotype.Component

@Component
class KafkaAdminHjesan(
    private val kafkaAdmin: KafkaAdmin
) {
    @Bean
    fun createTopics(): Unit {
        kafkaAdmin.setAutoCreate(true)
    }
}