package no.nav.syfo

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.prometheus.client.hotspot.DefaultExports
import no.nav.syfo.aktivermelding.AktiverMeldingService
import no.nav.syfo.aktivermelding.kafka.AktiverMelding
import no.nav.syfo.aktivermelding.kafka.AktiverMeldingKafkaProducer
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.db.Database
import no.nav.syfo.application.db.VaultCredentialService
import no.nav.syfo.application.util.JacksonKafkaSerializer
import no.nav.syfo.application.vault.RenewVaultService
import no.nav.syfo.kafka.loadBaseConfig
import no.nav.syfo.kafka.toProducerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.sparenajob")

fun main() {
    val env = Environment()
    val vaultSecrets = VaultSecrets()
    DefaultExports.initialize()
    val applicationState = ApplicationState()

    val vaultCredentialService = VaultCredentialService()
    val database = Database(env, vaultCredentialService)

    val kafkaBaseConfig = loadBaseConfig(env, vaultSecrets)
    val producerProperties = kafkaBaseConfig.toProducerConfig(env.applicationName, valueSerializer = JacksonKafkaSerializer::class)
    val aktiverMeldingKafkaProducer = AktiverMeldingKafkaProducer(env.aktiverMeldingTopic, KafkaProducer<String, AktiverMelding>(producerProperties))

    val aktiverMeldingService = AktiverMeldingService(database, aktiverMeldingKafkaProducer)

    val applicationEngine = embeddedServer(Netty, env.applicationPort) {}

    val applicationServer = ApplicationServer(applicationEngine, applicationState)
    applicationServer.start()
    applicationState.ready = true

    RenewVaultService(vaultCredentialService, applicationState).startRenewTasks()

    log.info("Starter jobb for å sende planlagte meldigner til Arena")
    aktiverMeldingService.start()

    log.info("Avslutter jobb")
    exitProcess(0)
}
