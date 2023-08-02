package no.nav.syfo

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.prometheus.client.hotspot.DefaultExports
import kotlin.system.exitProcess
import no.nav.syfo.aktivermelding.AktiverMeldingService
import no.nav.syfo.aktivermelding.kafka.AktiverMelding
import no.nav.syfo.aktivermelding.kafka.AktiverMeldingKafkaProducer
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.db.Database
import no.nav.syfo.application.util.JacksonKafkaSerializer
import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.sparenajob")

fun main() {
    val env = Environment()
    DefaultExports.initialize()
    val applicationState = ApplicationState()

    val database = Database(env)

    val producerProperties =
        KafkaUtils.getAivenKafkaConfig("aktiver-melding-producer")
            .toProducerConfig(env.applicationName, valueSerializer = JacksonKafkaSerializer::class)
    val aktiverMeldingKafkaProducer =
        AktiverMeldingKafkaProducer(
            env.aktiverMeldingTopic,
            KafkaProducer<String, AktiverMelding>(producerProperties)
        )

    val aktiverMeldingService = AktiverMeldingService(database, aktiverMeldingKafkaProducer)

    val applicationEngine = embeddedServer(Netty, env.applicationPort) {}

    val applicationServer = ApplicationServer(applicationEngine, applicationState)
    applicationServer.start()
    applicationState.ready = true

    log.info("Starter jobb for å sende planlagte meldinger til Arena")
    aktiverMeldingService.start()

    log.info("Avslutter jobb")
    exitProcess(0)
}
