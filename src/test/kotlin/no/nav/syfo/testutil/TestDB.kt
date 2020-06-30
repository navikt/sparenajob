package no.nav.syfo.testutil

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import java.sql.Connection
import java.sql.Timestamp
import no.nav.syfo.application.aktivermelding.db.PlanlagtMeldingDbModel
import no.nav.syfo.application.db.DatabaseInterface

class TestDB : DatabaseInterface {
    private var pg: EmbeddedPostgres? = null
    override val connection: Connection
        get() = pg!!.postgresDatabase.connection.apply { autoCommit = false }

    init {
        pg = EmbeddedPostgres.start()
    }

    fun stop() {
        pg?.close()
    }
}

fun Connection.setUp() {
    use { connection ->
        connection.prepareStatement(
            """
                CREATE TABLE planlagt_melding(
                    id UUID PRIMARY KEY,
                    fnr VARCHAR NOT NULL,
                    startdato DATE NOT NULL,
                    type VARCHAR NOT NULL,
                    opprettet timestamptz NOT NULL,
                    sendes timestamptz NOT NULL,
                    avbrutt timestamptz,
                    sendt timestamptz
                );
                """
        ).executeUpdate()
        connection.commit()
    }
}

fun Connection.dropData() {
    use { connection ->
        connection.prepareStatement("DELETE FROM planlagt_melding").executeUpdate()
        connection.commit()
    }
}

fun Connection.lagrePlanlagtMelding(planlagtMeldingDbModel: PlanlagtMeldingDbModel) {
    this.prepareStatement(
        """
            INSERT INTO planlagt_melding(
                id,
                fnr,
                startdato,
                type,
                opprettet,
                sendes,
                avbrutt,
                sendt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
             """
    ).use {
        it.setObject(1, planlagtMeldingDbModel.id)
        it.setString(2, planlagtMeldingDbModel.fnr)
        it.setObject(3, planlagtMeldingDbModel.startdato)
        it.setString(4, planlagtMeldingDbModel.type)
        it.setTimestamp(5, Timestamp.from(planlagtMeldingDbModel.opprettet.toInstant()))
        it.setTimestamp(6, Timestamp.from(planlagtMeldingDbModel.sendes.toInstant()))
        it.setTimestamp(7, if (planlagtMeldingDbModel.avbrutt != null) { Timestamp.from(planlagtMeldingDbModel.avbrutt?.toInstant()) } else { null })
        it.setTimestamp(8, if (planlagtMeldingDbModel.sendt != null) { Timestamp.from(planlagtMeldingDbModel.sendt?.toInstant()) } else { null })
        it.execute()
    }
    this.commit()
}
