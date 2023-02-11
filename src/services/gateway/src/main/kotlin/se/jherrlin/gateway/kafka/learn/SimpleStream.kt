package se.jherrlin.gateway.kafka.learn

import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.ForeachAction
import org.apache.kafka.streams.kstream.Produced
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


fun topology(streamProperties: Properties?): Topology? {
    val sysout = ForeachAction { key: String?, value: String -> println("key ${key}  value ${value}") }
    val stringSerde = Serdes.String()
    val builder = StreamsBuilder()
    builder.stream("kinaction_helloworld", Consumed.with(stringSerde, stringSerde))
        .peek(sysout)
        .mapValues { value: String -> value?.uppercase() }
        .peek(sysout)
        .to("kinaction_helloworld_uppercase", Produced.with(stringSerde, stringSerde))
    return builder.build(streamProperties)
}


fun main() {
    val kaProperties = Properties()
    kaProperties.put("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094")
    kaProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kaProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kaProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "yelling_app_id-101")
    kaProperties[AUTO_OFFSET_RESET_CONFIG] = "earliest"

    val top = topology(kaProperties)

    val doneLatch = CountDownLatch(1)

    KafkaStreams(top, kaProperties).use { kafkaStreams ->
            kafkaStreams.start()
            doneLatch.await(35000, TimeUnit.MILLISECONDS)
        }
}