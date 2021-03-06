package no.nav.syfo.aktivermelding

import java.time.OffsetDateTime
import java.util.UUID
import no.nav.syfo.aktivermelding.db.hentPlanlagteMeldingerSomSkalAktiveres
import no.nav.syfo.testutil.TestDB
import no.nav.syfo.testutil.dropData
import no.nav.syfo.testutil.lagPlanlagtMelding
import no.nav.syfo.testutil.lagrePlanlagtMelding
import no.nav.syfo.testutil.setUp
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object DbQueriesTest : Spek({
    val planlagtMeldingSkalSendesId = UUID.randomUUID()
    val planlagtMeldingSendtId = UUID.randomUUID()
    val planlagtMeldingAvbruttId = UUID.randomUUID()
    val testDb = TestDB()

    beforeGroup {
        testDb.connection.setUp()
    }

    afterEachTest {
        testDb.connection.dropData()
    }

    afterGroup {
        testDb.stop()
    }

    describe("Test av henting av planlagte meldinger som skal aktiveres") {
        it("Henter kun UUID for melding som skal aktiveres") {
            testDb.connection.lagrePlanlagtMelding(lagPlanlagtMelding(id = planlagtMeldingSendtId, sendt = OffsetDateTime.now().minusMinutes(30)))
            testDb.connection.lagrePlanlagtMelding(lagPlanlagtMelding(id = planlagtMeldingSkalSendesId))
            testDb.connection.lagrePlanlagtMelding(lagPlanlagtMelding(id = planlagtMeldingAvbruttId, avbrutt = OffsetDateTime.now().minusDays(15)))

            val planlagteMeldinger = testDb.hentPlanlagteMeldingerSomSkalAktiveres(OffsetDateTime.now())

            planlagteMeldinger.size shouldEqual 1
            planlagteMeldinger[0].id shouldEqual planlagtMeldingSkalSendesId
        }
        it("Henter ikke UUID for melding som skal aktiveres ennå") {
            testDb.connection.lagrePlanlagtMelding(lagPlanlagtMelding(id = planlagtMeldingSkalSendesId, sendes = OffsetDateTime.now().plusDays(2)))

            val planlagteMeldinger = testDb.hentPlanlagteMeldingerSomSkalAktiveres(OffsetDateTime.now())

            planlagteMeldinger.size shouldEqual 0
        }
    }
})
