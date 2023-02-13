package se.jherrlin.gateway.kafka.learn.state


import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.*
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


fun ktableWithReduceTopology(
    streamProperties: Properties,
    inputTopic: String,
    outputTopic: String,
): Topology {
    val sysout = { key: Any?, value: Any -> println("In ktableWithReduceTopology: key $key, value $value") }
    val builder = StreamsBuilder()
    val stringSerde = Serdes.String()
    val ktableWithReduce: KStream<String, String> = builder.stream(inputTopic, Consumed.with(stringSerde, stringSerde))
    ktableWithReduce
        .peek(sysout)
        .groupByKey()
        .reduce(
            { acc: String, value: String -> acc + value },
            Materialized.with(stringSerde, stringSerde))
        .toStream()
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

    val top = ktableWithReduceTopology(kaProperties, "kinaction_helloworld","kinaction_helloworld_ktableWithReduce")

    val doneLatch = CountDownLatch(1)

    KafkaStreams(top, kaProperties).use { kafkaStreams ->
        kafkaStreams.start()
        doneLatch.await(65, TimeUnit.SECONDS)
    }
}