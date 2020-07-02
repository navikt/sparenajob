package no.nav.syfo

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.hotspot.DefaultExports
import kotlin.system.exitProcess
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

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.sparenajob")

@KtorExperimentalAPI
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

    log.info("Jeg lever :D")
    aktiverMeldingService.start()

    log.info("Ferdig!")
    exitProcess(0)
}
