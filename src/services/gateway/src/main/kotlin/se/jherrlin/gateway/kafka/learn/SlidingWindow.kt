package se.jherrlin.gateway.kafka.learn

import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyWindowStore
import java.time.Duration
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


fun slidingWindowTopology(
    inputTopic: String,
    streamProperties: Properties,
    storeName: String,
): Topology {
    val sysout = ForeachAction { key: String?, value: String -> println("In slidingWindowTopology: key $key, value $value") }
    val builder = StreamsBuilder()
    val stringSerde = Serdes.String()
    val countStream: KStream<String, String> = builder.stream(inputTopic, Consumed.with(stringSerde, stringSerde))
    countStream
        .peek(sysout)
        .groupByKey()
        .windowedBy(SlidingWindows.ofTimeDifferenceAndGrace(Duration.ofSeconds(30), Duration.ofSeconds(1)))
        .count(Materialized.`as`(storeName))
    return builder.build(streamProperties)
}


fun main() {
    val kaProperties = Properties()
    kaProperties.put("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094")
    kaProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kaProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kaProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "yelling_app_id-101")
    kaProperties[AUTO_OFFSET_RESET_CONFIG] = "earliest"

    val top = slidingWindowTopology("kinaction_helloworld", kaProperties, "slidingWindowTopology")

    val doneLatch = CountDownLatch(1)

    KafkaStreams(top, kaProperties).use { kafkaStreams ->
        kafkaStreams.start()
        doneLatch.await(65, TimeUnit.SECONDS)
    }

//    val windowStore: ReadOnlyWindowStore<String, Long> = top.store("CountsWindowStore", QueryableStoreTypes.windowStore())
}