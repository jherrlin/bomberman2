import org.springframework.context.annotation.Bean


import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig.*
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.*
import java.time.Duration
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

val stringSerde = Serdes.String()

fun ktableCountAndSlidingWindowTopology(
    streamProperties: Properties,
    inputTopic: String,
    outputTopic: String,
): Topology {
    val sysout = { key: Any?, value: Any -> println("In ktableCountAndSlidingWindowTopology: key $key, value $value") }
    val builder = StreamsBuilder()

    val initializer = Initializer { "" }
    val aggregator = Aggregator { key: String, value: String, agg: String -> agg + value }
    val subaggregator = Aggregator { key: String, value: String, agg: String -> agg.replace(value, "") }

    builder.table(inputTopic, Consumed.with(stringSerde, stringSerde))
        .groupBy({key, value-> KeyValue.pair(key, value)},
            Grouped.with(stringSerde, stringSerde)
        )
        .aggregate(
            initializer,
            aggregator,
            subaggregator,
            Materialized.with(stringSerde, stringSerde)
        )
        .toStream()
        .peek(sysout)
        .to(outputTopic)

    return builder.build(streamProperties)
}

fun main() {
    val kaProperties = Properties()
    kaProperties["bootstrap.servers"] = "localhost:9092,localhost:9093,localhost:9094"
    kaProperties["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
    kaProperties["value.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
    kaProperties[APPLICATION_ID_CONFIG] = "app-02"
    kaProperties[AUTO_OFFSET_RESET_CONFIG] = "earliest"

    // Disable the cache and get values instantly instead of every 10MB or 30 sec
    kaProperties[CACHE_MAX_BYTES_BUFFERING_CONFIG] = 0

    val top = ktableCountAndSlidingWindowTopology(kaProperties, "s1","s1_ktableCountAndSlidingWindowTopology")

    val doneLatch = CountDownLatch(1)

    KafkaStreams(top, kaProperties).use { kafkaStreams ->
        kafkaStreams.start()
        doneLatch.await(125, TimeUnit.SECONDS)
    }
}



//class KtableAggregator {
//
//    @Bean
//    fun ktableConfig() {
//
//    }
//
//
//}