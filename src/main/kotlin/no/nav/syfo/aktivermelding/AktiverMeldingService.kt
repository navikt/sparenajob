package no.nav.syfo.aktivermelding

import java.time.OffsetDateTime
import no.nav.syfo.aktivermelding.db.hentPlanlagteMeldingerSomSkalAktiveres
import no.nav.syfo.aktivermelding.kafka.AktiverMelding
import no.nav.syfo.aktivermelding.kafka.AktiverMeldingKafkaProducer
import no.nav.syfo.application.db.DatabaseInterface
import no.nav.syfo.log

class AktiverMeldingService(
    private val database: DatabaseInterface,
    private val aktiverMeldingKafkaProducer: AktiverMeldingKafkaProducer
) {
    fun start() {
        val aktiverMeldinger = database.hentPlanlagteMeldingerSomSkalAktiveres(OffsetDateTime.now())
            .map { AktiverMelding(it) }

        log.info("Sender {} meldinger til aktivering", aktiverMeldinger.size)
        aktiverMeldinger.forEach { aktiverMeldingKafkaProducer.publishToKafka(it) }
    }
}
