package se.jherrlin.bomberman.gateway.streams

import mu.KotlinLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
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
import java.time.LocalDateTime
import java.time.ZoneId


@Configuration(proxyBeanMethods = false)
@EnableKafkaStreams
class KeyCountSlidingWindowed(
    val factoryBean: StreamsBuilderFactoryBean
) {
    val stringSerde = Serdes.String()
    val longSerde = Serdes.Long()
    val STORE_NAME = "WORD_COUNT_SLIDING_WINDOWED_STREAM_STORE"
    val logger = KotlinLogging.logger {}
    val sysout1 = ForeachAction { key: String?, value: String ->
        logger.debug { "Got in WordCountWindowed stream: key ${key}  value ${value}" }
    }

    @Bean
    fun keyCountWindowedTopology(streamsBuilder: StreamsBuilder): KStream<Windowed<String>, Long>? {
        val stream = streamsBuilder
            .stream("s1", Consumed.with(stringSerde, stringSerde))
            .peek(sysout1)
            .mapValues { it -> it.lowercase() }
            .flatMapValues { text -> text.split("\\W+") }
            .groupBy({key, value -> value}, Grouped.with(stringSerde, stringSerde))
            .windowedBy(SlidingWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(30)))
            .count(Materialized.`as`(STORE_NAME))
            .toStream()

        stream
            .map { windowedKey, value -> KeyValue.pair(windowedKey.key(), value.toString()) }
            .to("s2", Produced.with(stringSerde, stringSerde))

        return stream
    }

    fun instToLDT(instant: Instant) = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    fun epocToLDT(t: Long)= instToLDT(Instant.ofEpochMilli(t))

    fun queryKeyCountSlidingWindowedStore(word: String): Long {
        val streams: KafkaStreams = factoryBean.kafkaStreams!!
        val timeTo = Instant.now()
        val timeFrom = Instant.now().minusSeconds(30)

        val windowKeyQuery: WindowKeyQuery<String, ValueAndTimestamp<Long>> =
            WindowKeyQuery.withKeyAndWindowStartRange(word, timeFrom, timeTo)

        val query = inStore(STORE_NAME).withQuery(windowKeyQuery)
        val result = streams.query(query)

//        try {
//            val r = result.onlyPartitionResult.result.asSequence().first()
//            println("r:                   $r")
//            println("key:                 $word")
//            println("r.key:               ${epocToLDT(r.key)}")
//            println("r.value.value():     ${r.value.value()}")
//            println("r.value.timestamp(): ${epocToLDT(r.value.timestamp())}")
//        } catch (e: java.lang.Exception) {
//            logger.error(e) {"Could not get word form store"}
//        }

        return try {
            val store = streams.store(
                StoreQueryParameters.fromNameAndType(
                    STORE_NAME, QueryableStoreTypes.windowStore<String, Long>()))
            val tsFrom = LocalDateTime.ofInstant(timeFrom, ZoneId.systemDefault())
            val tsTo = LocalDateTime.ofInstant(timeTo, ZoneId.systemDefault())
            println("Counting '$word' in window $tsFrom - $tsTo")
            val iterator = store.fetch(word, timeFrom, timeTo);
            while (iterator.hasNext()) {
                val next = iterator.next()
                val windowTimestamp = next.key;
                val ts = LocalDateTime.ofInstant(Instant.ofEpochMilli(windowTimestamp), ZoneId.systemDefault())
                println("Count of '$word' @ time " + ts + " is " + next.value);
            }

            iterator.close()
            0
        } catch (e: Exception) {
            logger.error(e) {"Could not get word form store"}
            -1L
        }
    }
}