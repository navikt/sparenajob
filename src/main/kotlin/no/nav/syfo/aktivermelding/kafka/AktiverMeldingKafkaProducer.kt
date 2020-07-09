package no.nav.syfo.aktivermelding.kafka

import no.nav.syfo.log
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class AktiverMeldingKafkaProducer(
    private val aktiverMeldingTopic: String,
    private val kafkaProducer: KafkaProducer<String, AktiverMelding>
) {

    fun publishToKafka(aktiverMelding: AktiverMelding) {
        try {
            kafkaProducer.send(ProducerRecord(aktiverMeldingTopic, aktiverMelding)).get()
        } catch (e: Exception) {
            log.error("Kunne ikke skrive til topic {}", e.message)
            throw e
        }
    }
}
