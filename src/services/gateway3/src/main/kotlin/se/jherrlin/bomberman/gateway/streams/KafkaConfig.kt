package se.jherrlin.bomberman.gateway.streams

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder


@Configuration
class KafkaConfig {
    @Bean
    fun createTopicS1(): NewTopic =
        TopicBuilder.name("s1")
            .partitions(1)
            .replicas(3)
            .build()

    @Bean
    fun createTopicS2(): NewTopic =
        TopicBuilder.name("s2")
            .partitions(1)
            .replicas(3)
            .build()

    @Bean
    fun createTopicS3(): NewTopic =
        TopicBuilder.name("s3")
            .partitions(1)
            .replicas(3)
            .build()
}