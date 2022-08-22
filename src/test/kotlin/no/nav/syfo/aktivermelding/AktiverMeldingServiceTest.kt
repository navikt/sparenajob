package no.nav.syfo.aktivermelding

import io.kotest.core.spec.style.FunSpec
import no.nav.syfo.aktivermelding.db.AKTIVITETSKRAV_8_UKER_TYPE
import no.nav.syfo.aktivermelding.db.BREV_39_UKER_TYPE
import no.nav.syfo.aktivermelding.db.BREV_4_UKER_TYPE
import no.nav.syfo.testutil.lagPlanlagtMelding
import org.amshove.kluent.shouldBeEqualTo
import java.time.OffsetDateTime
import java.util.UUID

class AktiverMeldingServiceTest : FunSpec({
    val planlagtMelding4uker = UUID.randomUUID()
    val planlagtMelding8uker = UUID.randomUUID()
    val planlagtMelding39uker = UUID.randomUUID()

    context("Filtrering av meldinger med samme fnr som aktiveres samtidig") {
        test("Filtrerer bort 8- og 39-ukersmelding") {
            val planlagteMeldinger = listOf(
                lagPlanlagtMelding(
                    id = planlagtMelding8uker,
                    type = AKTIVITETSKRAV_8_UKER_TYPE,
                    opprettet = OffsetDateTime.now().minusNanos(20),
                    sendes = OffsetDateTime.now().minusDays(20)
                ),
                lagPlanlagtMelding(
                    id = planlagtMelding39uker,
                    type = BREV_39_UKER_TYPE,
                    opprettet = OffsetDateTime.now().minusNanos(10),
                    sendes = OffsetDateTime.now().minusDays(10)
                ),
                lagPlanlagtMelding(
                    id = planlagtMelding4uker,
                    type = BREV_4_UKER_TYPE,
                    opprettet = OffsetDateTime.now().minusNanos(30),
                    sendes = OffsetDateTime.now().minusDays(30)
                )
            )

            val filtrerteMeldinger = filtrerBortPlanlagteMeldingerForSammeFnr(planlagteMeldinger)

            filtrerteMeldinger.size shouldBeEqualTo 1
            filtrerteMeldinger[0].type shouldBeEqualTo BREV_4_UKER_TYPE
        }
        test("Filtrerer bort 39-ukersmelding") {
            val planlagteMeldinger = listOf(
                lagPlanlagtMelding(
                    id = planlagtMelding8uker,
                    type = AKTIVITETSKRAV_8_UKER_TYPE,
                    opprettet = OffsetDateTime.now().minusNanos(20),
                    sendes = OffsetDateTime.now().minusDays(20)
                ),
                lagPlanlagtMelding(
                    id = planlagtMelding39uker,
                    type = BREV_39_UKER_TYPE,
                    opprettet = OffsetDateTime.now().minusNanos(10),
                    sendes = OffsetDateTime.now().minusDays(10)
                )
            )

            val filtrerteMeldinger = filtrerBortPlanlagteMeldingerForSammeFnr(planlagteMeldinger)

            filtrerteMeldinger.size shouldBeEqualTo 1
            filtrerteMeldinger[0].type shouldBeEqualTo AKTIVITETSKRAV_8_UKER_TYPE
        }
        test("Filtrerer ikke bort meldinger med ulikt fnr") {
            val planlagteMeldinger = listOf(
                lagPlanlagtMelding(
                    id = planlagtMelding8uker,
                    fnr = "fnr1",
                    type = AKTIVITETSKRAV_8_UKER_TYPE,
                    sendes = OffsetDateTime.now().minusMinutes(20)
                ),
                lagPlanlagtMelding(
                    id = planlagtMelding39uker,
                    fnr = "fnr2",
                    type = BREV_39_UKER_TYPE,
                    sendes = OffsetDateTime.now().minusMinutes(10)
                )
            )

            val filtrerteMeldinger = filtrerBortPlanlagteMeldingerForSammeFnr(planlagteMeldinger)

            filtrerteMeldinger.size shouldBeEqualTo 2
            filtrerteMeldinger[0].fnr shouldBeEqualTo "fnr1"
            filtrerteMeldinger[1].fnr shouldBeEqualTo "fnr2"
        }
        test("Feiler ikke ved tom liste") {
            val filtrerteMeldinger = filtrerBortPlanlagteMeldingerForSammeFnr(emptyList())

            filtrerteMeldinger.size shouldBeEqualTo 0
        }
    }
})
