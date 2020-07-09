package no.nav.syfo.aktivermelding.db

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.util.UUID
import no.nav.syfo.application.db.DatabaseInterface
import no.nav.syfo.application.db.toList

fun DatabaseInterface.hentPlanlagteMeldingerSomSkalAktiveres(now: OffsetDateTime): List<UUID> {
    connection.use { connection ->
        return connection.hentPlanlagtMelding(now)
    }
}

fun Connection.hentPlanlagtMelding(now: OffsetDateTime): List<UUID> =
    this.prepareStatement(
        """
            SELECT id FROM planlagt_melding WHERE sendes<? AND sendt is null and avbrutt is null;
            """
    ).use {
        it.setTimestamp(1, Timestamp.from(now.toInstant()))
        it.executeQuery().toList { toUuid() }
    }

fun ResultSet.toUuid(): UUID =
    getObject("id", UUID::class.java)
