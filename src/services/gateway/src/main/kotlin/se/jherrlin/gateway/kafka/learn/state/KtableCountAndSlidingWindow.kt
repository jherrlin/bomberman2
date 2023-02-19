package se.jherrlin.gateway.kafka.learn.state


import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig.*
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.SlidingWindows
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
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
    builder.stream(inputTopic, Consumed.with(stringSerde, stringSerde))
        .peek(sysout)
        .groupByKey()
        // Sliding window. Window 30 sec, update every 1 sec.
        .windowedBy(SlidingWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(30)))
        .count(Materialized.`as`("count-and-sliding-window-store"))
        .toStream()
        .map { windowedKey, value -> KeyValue.pair(windowedKey.key(), value.toString()) }
        .peek(sysout)
        .to(outputTopic, Produced.with(stringSerde, stringSerde))
    return builder.build(streamProperties)
}

fun main() {
    val kaProperties = Properties()
    kaProperties["bootstrap.servers"] = "localhost:9092,localhost:9093,localhost:9094"
    kaProperties["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
    kaProperties["value.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
    kaProperties[APPLICATION_ID_CONFIG] = "app-021"
    kaProperties[AUTO_OFFSET_RESET_CONFIG] = "earliest"

    // Disable the cache and get values instantly instead of every 10MB or 30 sec
    kaProperties[CACHE_MAX_BYTES_BUFFERING_CONFIG] = 0

    val top = ktableCountAndSlidingWindowTopology(kaProperties, "s1","s1_ktableCountAndSlidingWindowTopology")

    val doneLatch = CountDownLatch(1)

    KafkaStreams(top, kaProperties).use { kafkaStreams ->
        kafkaStreams.start()

//        kafkaStreams.store("count-and-sliding-window-store")

        doneLatch.await(125, TimeUnit.SECONDS)


    }
}


