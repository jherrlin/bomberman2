package se.jherrlin.gateway.kafka.learn.state.derp

import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG
import org.apache.kafka.streams.StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.WindowStore
import java.time.Duration
import java.util.*


val stringSerde = Serdes.String()


fun windowStreamTopology(builder: StreamsBuilder, inputTopic: String, outputTopic: String): StreamsBuilder {
    val initializer = Initializer { "" }
    val aggregator = Aggregator { key: String, value: String, agg: String -> agg + value }

    val materialized: Materialized<String, String, WindowStore<Bytes, ByteArray>> = Materialized
        .`as`<String, String, WindowStore<Bytes, ByteArray>>("A-STREAM-STORE")
        .withKeySerde(stringSerde)
        .withValueSerde(stringSerde)

    builder
        .stream(inputTopic, Consumed.with(stringSerde, stringSerde))
        .groupByKey()
        .windowedBy(SlidingWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(30)))
        .aggregate(initializer, aggregator, materialized)

    return builder
}

fun props(): Properties{
    val kaProperties = Properties()
    kaProperties["bootstrap.servers"] = "localhost:9092,localhost:9093,localhost:9094"
    kaProperties["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
    kaProperties["value.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
    kaProperties[APPLICATION_ID_CONFIG] = "app-02he11"
    kaProperties[AUTO_OFFSET_RESET_CONFIG] = "earliest"
    // Disable the cache and get values instantly instead of every 10MB or 30 sec
    kaProperties[CACHE_MAX_BYTES_BUFFERING_CONFIG] = 0
    return kaProperties
}

fun main() {
    val builder = StreamsBuilder()
    val properties = props()
    val topBuilder = windowStreamTopology(builder, "s1","s1_ktableCountAndSlidingWindowTopology")
    val top = topBuilder.build(properties)

    println(top.describe().toString())

    val streams = KafkaStreams(top, properties)

    streams.start()

    val store = streams.store(
        StoreQueryParameters.fromNameAndType(
            "A-STREAM-STORE",
            QueryableStoreTypes.keyValueStore<String, String>()
        )
    )

    print("Store query:")
    println(store.get("h"))

    streams.cleanUp()
    streams.close()

}