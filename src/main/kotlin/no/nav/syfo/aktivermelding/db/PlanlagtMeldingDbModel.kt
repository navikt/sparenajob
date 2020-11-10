package no.nav.syfo.aktivermelding.db

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

const val BREV_4_UKER_TYPE = "4UKER"
const val AKTIVITETSKRAV_8_UKER_TYPE = "8UKER"
const val BREV_39_UKER_TYPE = "39UKER"

data class PlanlagtMeldingDbModel(
    val id: UUID,
    val fnr: String,
    val startdato: LocalDate,
    val type: String,
    val opprettet: OffsetDateTime,
    val sendes: OffsetDateTime,
    val avbrutt: OffsetDateTime? = null,
    val sendt: OffsetDateTime? = null
)
