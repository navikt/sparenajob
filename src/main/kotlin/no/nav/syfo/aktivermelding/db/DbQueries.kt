package no.nav.syfo.aktivermelding.db

import no.nav.syfo.application.db.DatabaseInterface
import no.nav.syfo.application.db.toList
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

fun DatabaseInterface.hentPlanlagteMeldingerSomSkalAktiveres(now: OffsetDateTime): List<PlanlagtMeldingDbModel> {
    connection.use { connection ->
        return connection.hentPlanlagtMelding(now)
    }
}

fun Connection.hentPlanlagtMelding(now: OffsetDateTime): List<PlanlagtMeldingDbModel> =
    this.prepareStatement(
        """
            SELECT * FROM planlagt_melding WHERE sendes<? AND sendt is null and avbrutt is null;
            """
    ).use {
        it.setTimestamp(1, Timestamp.from(now.toInstant()))
        it.executeQuery().toList { toPlanlagtMeldingDbModel() }
    }

fun ResultSet.toPlanlagtMeldingDbModel(): PlanlagtMeldingDbModel =
    PlanlagtMeldingDbModel(
        id = getObject("id", UUID::class.java),
        fnr = getString("fnr"),
        startdato = getObject("startdato", LocalDate::class.java),
        type = getString("type"),
        opprettet = getTimestamp("opprettet").toInstant().atOffset(ZoneOffset.UTC),
        sendes = getTimestamp("sendes").toInstant().atOffset(ZoneOffset.UTC),
        avbrutt = getTimestamp("avbrutt")?.toInstant()?.atOffset(ZoneOffset.UTC),
        sendt = getTimestamp("sendt")?.toInstant()?.atOffset(ZoneOffset.UTC)
    )
