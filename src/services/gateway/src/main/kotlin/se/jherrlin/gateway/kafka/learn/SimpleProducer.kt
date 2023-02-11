package se.jherrlin.gateway.kafka.learn

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.*


fun main() {
    val kaProperties = Properties()
    kaProperties.put("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094")
    kaProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kaProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val topicName = "kinaction_helloworld"
    try {
        val producer: Producer<String, String> = KafkaProducer(kaProperties)
        val producerRecord = ProducerRecord<String?, String>(topicName, null, "hello from bomberman2")
        producer.send(producerRecord)
        producer.close()
    } catch (e: Exception) {
        println(e)
    }
}