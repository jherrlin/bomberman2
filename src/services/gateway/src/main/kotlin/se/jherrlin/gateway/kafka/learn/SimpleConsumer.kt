package se.jherrlin.gateway.kafka.learn

import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.util.*


class SimpleConsumer {
    @Volatile
    private var keepConsuming = true
    var topicName = "kinaction_helloworld_uppercase"
    private fun shutDown() {
        keepConsuming = false
    }

    private fun consume(kaProperties: Properties) {
        KafkaConsumer<String, String>(kaProperties).use { consumer ->
            consumer.subscribe(
                listOf(topicName)
            )
            while (keepConsuming) {
                val records =
                    consumer.poll(Duration.ofMillis(250))
                for (record in records) {
                    print("kinaction_info offset = ")
                    println(record.offset())
                    print("kinaction_value = ")
                    println(record.value())
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val kaProperties = Properties()
            kaProperties[BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092,localhost:9093,localhost:9094"
            kaProperties[GROUP_ID_CONFIG] = "kinaction_helloconsumer"
            kaProperties[ENABLE_AUTO_COMMIT_CONFIG] = "true"
            kaProperties[AUTO_COMMIT_INTERVAL_MS_CONFIG] = "1000"
            kaProperties[KEY_DESERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringDeserializer"
            kaProperties[VALUE_DESERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringDeserializer"
            kaProperties[AUTO_OFFSET_RESET_CONFIG] = "earliest"
            val simpleConsumer = SimpleConsumer()
            simpleConsumer.consume(kaProperties)
            Runtime.getRuntime().addShutdownHook(Thread { simpleConsumer.shutDown() })
        }
    }
}
