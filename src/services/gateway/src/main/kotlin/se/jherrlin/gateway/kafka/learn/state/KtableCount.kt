package se.jherrlin.gateway.kafka.learn.state


import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.*
import org.apache.kafka.streams.StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG
import org.apache.kafka.streams.kstream.*
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


fun ktableCountTopology(
    streamProperties: Properties,
    inputTopic: String,
    outputTopic: String,
): Topology {
    val sysout = { key: Any?, value: Any -> println("In ktableCountTopology: key $key, value $value") }
    val builder = StreamsBuilder()
    val stringSerde = Serdes.String()
    builder.stream(inputTopic, Consumed.with(stringSerde, stringSerde))
        .peek(sysout)
        .groupByKey()
        // Sliding window. Window 15 sec, update every 1 sec.
//        .windowedBy(SlidingWindows.ofTimeDifferenceAndGrace(Duration.ofSeconds(10), Duration.ofSeconds(1)))
        .count(Materialized.`as`("counting-store"))
        .toStream()
//        .map({ (key, value) -> KeyValue.pair(key.key(), value) })
        .mapValues {  value -> value.toString()  }
        .peek(sysout)
        .to(outputTopic, Produced.with(stringSerde, stringSerde))
    return builder.build(streamProperties)
}

fun main() {
    val kaProperties = Properties()
    kaProperties.put("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094")
    kaProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kaProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kaProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "yelling_app_id-101")
    kaProperties[AUTO_OFFSET_RESET_CONFIG] = "earliest"

    // Disable the cache and get values instantly instead of every 10MB or 30 sec
    kaProperties[CACHE_MAX_BYTES_BUFFERING_CONFIG] = 0

    val top = ktableCountTopology(kaProperties, "kinaction_helloworld","kinaction_helloworld_ktableCountTopology")

    val doneLatch = CountDownLatch(1)

    KafkaStreams(top, kaProperties).use { kafkaStreams ->
        kafkaStreams.start()
        doneLatch.await(65, TimeUnit.SECONDS)
    }
}