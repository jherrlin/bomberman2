package se.jherrlin.bomberman.gateway.streams

import mu.KotlinLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import java.time.Duration
import java.time.Instant

@Configuration(proxyBeanMethods = false)
@EnableKafkaStreams
class WordCountWindowed(
    val factoryBean: StreamsBuilderFactoryBean
) {
    val stringSerde = Serdes.String()
    val STORE_NAME = "WORD_COUNT_WINDOWED_STREAM_STORE"
    val logger = KotlinLogging.logger {}
    val sysout1 = ForeachAction { key: String?, value: String -> println("Got in WordCountWindowed stream: key ${key}  value ${value}") }

    @Bean
    fun wordCountWindowedTopology(streamsBuilder: StreamsBuilder): KTable<Windowed<String>, Long> {
        val messageStream: KStream<String, String> = streamsBuilder
            .stream("s1", Consumed.with(stringSerde, stringSerde))

        val wordCounts = messageStream
            .peek(sysout1)
            .mapValues { it -> it.lowercase() }
            .flatMapValues { text -> text.split("\\W+") }
            .groupByKey()
            .windowedBy(SlidingWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(30)))
            .count(Materialized.`as`(STORE_NAME))

        return wordCounts
    }

    fun queryWordCountWindowedStore(word: String): Long {
        val streams: KafkaStreams = factoryBean.kafkaStreams!!
        val timeTo = Instant.now()
        val timeFrom = Instant.ofEpochMilli(0)

        logger.info { "Stream app state: ${streams.state()}" }

        val store = streams.store(
            StoreQueryParameters.fromNameAndType(
                STORE_NAME, QueryableStoreTypes.windowStore<String, Long>()))

        return try {
            val iterator = store.fetch(word, timeFrom, timeTo);
            while (iterator.hasNext()) {
                val next = iterator.next()
                val windowTimestamp = next.key;
                println("Count of 'world' @ time " + windowTimestamp + " is " + next.value);
            }

            iterator.close()
            -1L
        } catch (e: Exception) {
            logger.error(e) {"Could not get word form store"}
            -2L
        }

    }
}