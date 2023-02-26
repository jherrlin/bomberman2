package se.jherrlin.bomberman.gateway

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
import org.springframework.stereotype.Component

import se.jherrlin.bomberman.models.Counter
import java.time.Duration
import java.time.LocalDate

@Configuration(proxyBeanMethods = false)
@EnableKafkaStreams
class StreamCounter {
    val sysout1 = ForeachAction { key: String?, value: String -> println("After: key ${key}  value ${value}") }
    val stringSerde = Serdes.String()
    val sysout = { key: Any?, value: Any -> println("Before: key $key, value $value") }

    val aggregator = Aggregator { key: String, value: String, agg: Counter -> Counter(agg.count++, LocalDate.now().toString()) }

//    val materialized: Materialized<String, Counter, WindowStore<Bytes, ByteArray>> = Materialized
//        .`as`<String, Counter, WindowStore<Bytes, ByteArray>>("A-STREAM-STORE")
//        .withKeySerde(stringSerde)
//        .withValueSerde(Counter.)

//    @Bean
//    fun kStream1(streamsBuilder: StreamsBuilder): KStream<String, String> {
//        val stream = streamsBuilder.stream("s1", Consumed.with(stringSerde, stringSerde))
//        stream
//            .peek(sysout)
//            .groupByKey()
//            .windowedBy(SlidingWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(30)))
//            .count(Materialized.`as`("count-and-sliding-window-store"))
//
//        return stream
//    }


//    @Bean
//    fun kTable(streamsBuilder: StreamsBuilder): KTable<String, Long> =
//        streamsBuilder
//            .table("s2", Consumed.with(stringSerde, stringSerde))
//            .groupBy({key, value-> KeyValue.pair(key, value)},
//                Grouped.with(stringSerde, stringSerde)
//            )
//            .count(Materialized.`as`("counting-store"))

    private fun uppercaseValue(key: String, value: String): KeyValue<String?, String?> {
        return KeyValue(key, value.uppercase())
    }
}


@Component
class StreamCounterController(
    val factoryBean: StreamsBuilderFactoryBean
) {
    fun queryStore(key: String): Long {
        val kafkaStreams: KafkaStreams? = factoryBean.getKafkaStreams()
        val store = kafkaStreams!!.store(
            StoreQueryParameters.fromNameAndType(
                "counting-store", QueryableStoreTypes.keyValueStore<String, Long>()
            )
        )
        return store.get(key);
    }
}