package no.nav.syfo.aktivermelding

import io.kotest.core.spec.style.FunSpec
import java.time.OffsetDateTime
import java.util.UUID
import no.nav.syfo.aktivermelding.db.hentPlanlagteMeldingerSomSkalAktiveres
import no.nav.syfo.testutil.TestDB
import no.nav.syfo.testutil.dropData
import no.nav.syfo.testutil.lagPlanlagtMelding
import no.nav.syfo.testutil.lagrePlanlagtMelding
import no.nav.syfo.testutil.setUp
import org.amshove.kluent.shouldBeEqualTo

class DbQueriesTest :
    FunSpec({
        val planlagtMeldingSkalSendesId = UUID.randomUUID()
        val planlagtMeldingSendtId = UUID.randomUUID()
        val planlagtMeldingAvbruttId = UUID.randomUUID()
        val testDb = TestDB.database

        beforeSpec { testDb.connection.setUp() }

        afterTest { testDb.connection.dropData() }

        context("Test av henting av planlagte meldinger som skal aktiveres") {
            test("Henter kun UUID for melding som skal aktiveres") {
                testDb.connection.lagrePlanlagtMelding(
                    lagPlanlagtMelding(
                        id = planlagtMeldingSendtId,
                        sendt = OffsetDateTime.now().minusMinutes(30)
                    )
                )
                testDb.connection.lagrePlanlagtMelding(
                    lagPlanlagtMelding(id = planlagtMeldingSkalSendesId)
                )
                testDb.connection.lagrePlanlagtMelding(
                    lagPlanlagtMelding(
                        id = planlagtMeldingAvbruttId,
                        avbrutt = OffsetDateTime.now().minusDays(15)
                    )
                )

                val planlagteMeldinger =
                    testDb.hentPlanlagteMeldingerSomSkalAktiveres(OffsetDateTime.now())

                planlagteMeldinger.size shouldBeEqualTo 1
                planlagteMeldinger[0].id shouldBeEqualTo planlagtMeldingSkalSendesId
            }
            test("Henter ikke UUID for melding som skal aktiveres ennå") {
                testDb.connection.lagrePlanlagtMelding(
                    lagPlanlagtMelding(
                        id = planlagtMeldingSkalSendesId,
                        sendes = OffsetDateTime.now().plusDays(2)
                    )
                )

                val planlagteMeldinger =
                    testDb.hentPlanlagteMeldingerSomSkalAktiveres(OffsetDateTime.now())

                planlagteMeldinger.size shouldBeEqualTo 0
            }
        }
    })
