package se.jherrlin.bomberman.gateway.streams

import mu.KotlinLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.query.KeyQuery
import org.apache.kafka.streams.query.StateQueryRequest.inStore
import org.apache.kafka.streams.query.WindowKeyQuery
import org.apache.kafka.streams.query.WindowRangeQuery
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ValueAndTimestamp
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import java.time.Duration
import java.time.Instant


@Configuration(proxyBeanMethods = false)
@EnableKafkaStreams
class KeyCountSlidingWindowed(
    val factoryBean: StreamsBuilderFactoryBean
) {
    val stringSerde = Serdes.String()
    val STORE_NAME = "WORD_COUNT_SLIDING_WINDOWED_STREAM_STORE"
    val logger = KotlinLogging.logger {}
    val sysout1 = ForeachAction { key: String?, value: String -> println("Got in WordCountWindowed stream: key ${key}  value ${value}") }

    @Bean
    fun keyCountWindowedTopology(streamsBuilder: StreamsBuilder): KTable<Windowed<String>, Long> {
        val messageStream: KStream<String, String> = streamsBuilder
            .stream("s1", Consumed.with(stringSerde, stringSerde))

        return messageStream
            .peek(sysout1)
            .groupByKey()
            .windowedBy(SlidingWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(30)))
            .count(Materialized.`as`(STORE_NAME))
    }

    fun queryKeyCountSlidingWindowedStore(word: String): Long {
        val streams: KafkaStreams = factoryBean.kafkaStreams!!
        val timeTo = Instant.now()
        val timeFrom = Instant.now().minusSeconds(30)

        val keyQuery: KeyQuery<String, ValueAndTimestamp<Long>> =
            KeyQuery.withKey(word);

        val windowKeyQuery: WindowKeyQuery<String, ValueAndTimestamp<Long>> =
            WindowKeyQuery.withKeyAndWindowStartRange(word, timeFrom, timeTo)

        WindowRangeQuery.withWindowStartRange(timeFrom, timeTo)

        val query = inStore(STORE_NAME).withQuery(windowKeyQuery)
        val result = streams.query(query)


        try {
            val r = result.onlyPartitionResult.result.iterator().asSequence().first()
            println("r.key:               ${r.key}")
            println("r.value.value():     ${r.value.value()}")
            println("r.value.timestamp(): ${r.value.timestamp()}")
        } catch (e: java.lang.Exception) {
            logger.error(e) {"Could not get word form store"}
        }


//        println("In here!")
//
//        logger.info { "Stream app state: ${streams.state()}" }

        val store = streams.store(
            StoreQueryParameters.fromNameAndType(
                STORE_NAME, QueryableStoreTypes.windowStore<String, Long>()))

        return try {
//            val iterator = store.fetch(word, timeFrom, timeTo);
//            while (iterator.hasNext()) {
//                val next = iterator.next()
//                val windowTimestamp = next.key;
//                println("Count of '$word' @ time " + windowTimestamp + " is " + next.value);
//            }
//
//            iterator.close()
            -1L
        } catch (e: Exception) {
            logger.error(e) {"Could not get word form store"}
            -2L
        }

    }
}