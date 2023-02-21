package se.jherrlin.gateway

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.ForeachAction
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.support.serializer.JsonSerde

@Configuration(proxyBeanMethods = false)
@EnableKafkaStreams
class MyStream {
    val sysout1 = ForeachAction { key: String?, value: String -> println("After: key ${key}  value ${value}") }
    val stringSerde = Serdes.String()
        val sysout = { key: Any?, value: Any -> println("Before: key $key, value $value") }

    @Bean
    fun kStream(streamsBuilder: StreamsBuilder): KStream<String, String> {
        val stream = streamsBuilder.stream("s1", Consumed.with(stringSerde, stringSerde))
        stream
            .peek(sysout)
            .map(this::uppercaseValue)
            .peek(sysout1)
            .to("s1_uppercase", Produced.with(stringSerde, stringSerde))
        return stream
    }

    private fun uppercaseValue(key: String, value: String): KeyValue<String?, String?> {
        return KeyValue(key, value.uppercase())
    }

}

