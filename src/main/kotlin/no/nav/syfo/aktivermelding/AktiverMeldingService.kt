package no.nav.syfo.aktivermelding

import java.time.OffsetDateTime
import no.nav.syfo.aktivermelding.db.PlanlagtMeldingDbModel
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
        val aktiverMeldinger = filtrerBortPlanlagteMeldingerForSammeFnr(database.hentPlanlagteMeldingerSomSkalAktiveres(OffsetDateTime.now()))
            .map { AktiverMelding(it.id) }

        log.info("Sender {} meldinger til aktivering", aktiverMeldinger.size)
        aktiverMeldinger.forEach { aktiverMeldingKafkaProducer.publishToKafka(it) }
    }
}

// Det feiler i Arena hvis vi sender f.eks. 4-ukersmelding og 8-ukersmelding for samme fnr for tett på hverandre
fun filtrerBortPlanlagteMeldingerForSammeFnr(planlagteMeldinger: List<PlanlagtMeldingDbModel>): List<PlanlagtMeldingDbModel> {
    return planlagteMeldinger.groupBy { it.fnr }.entries.mapNotNull { it.value.minBy { planlagtMelding -> planlagtMelding.sendes } }
}
