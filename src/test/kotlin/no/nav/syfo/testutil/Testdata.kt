package no.nav.syfo.testutil

import no.nav.syfo.aktivermelding.db.AKTIVITETSKRAV_8_UKER_TYPE
import no.nav.syfo.aktivermelding.db.PlanlagtMeldingDbModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun lagPlanlagtMelding(
    id: UUID,
    fnr: String = "fnr",
    type: String = AKTIVITETSKRAV_8_UKER_TYPE,
    opprettet: OffsetDateTime = OffsetDateTime.now().minusWeeks(7),
    sendes: OffsetDateTime = OffsetDateTime.now().minusHours(5),
    sendt: OffsetDateTime? = null,
    avbrutt: OffsetDateTime? = null
): PlanlagtMeldingDbModel {
    return PlanlagtMeldingDbModel(
        id = id,
        fnr = fnr,
        startdato = LocalDate.now().minusMonths(2),
        type = type,
        opprettet = opprettet,
        sendes = sendes,
        avbrutt = avbrutt,
        sendt = sendt
    )
}
