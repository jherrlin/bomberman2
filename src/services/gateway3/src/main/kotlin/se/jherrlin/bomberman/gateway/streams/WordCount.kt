package se.jherrlin.bomberman.gateway.streams

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import mu.KotlinLogging
import java.lang.Exception


@Configuration(proxyBeanMethods = false)
@EnableKafkaStreams
class WordCount(
    val factoryBean: StreamsBuilderFactoryBean
) {
    val stringSerde = Serdes.String()
    val COUNT_STORE = "WORD_COUNT_STREAM_STORE"
    val logger = KotlinLogging.logger {}
    val sysout1 = ForeachAction { key: String?, value: String ->
        logger.debug { "Got in WordCount stream: key ${key}  value ${value}" }
    }

    @Bean
    fun wordCountTopology(streamsBuilder: StreamsBuilder): KTable<String, Long> {
        val messageStream: KStream<String, String> = streamsBuilder
            .stream("s1", Consumed.with(stringSerde, stringSerde))

        val wordCounts = messageStream
            .peek(sysout1)
            .mapValues { it -> it.lowercase() }
            .flatMapValues { text -> text.split("\\W+") }
            .groupBy({key, value -> value}, Grouped.with(stringSerde, stringSerde))
            .count(Materialized.`as`(COUNT_STORE))

        return wordCounts
    }

    fun queryWordCountStore(word: String): Long {
        val kafkaStreams: KafkaStreams = factoryBean.kafkaStreams!!
        logger.debug { "Stream app state: ${kafkaStreams.state()}" }
        val store = kafkaStreams.store(
            StoreQueryParameters.fromNameAndType(
                COUNT_STORE, QueryableStoreTypes.keyValueStore<String, Long>()))
        return try {
            store.get(word);
        } catch (e: Exception) {
            logger.error(e) {"Could not get word from: $COUNT_STORE"}
            0L
        }
    }
}