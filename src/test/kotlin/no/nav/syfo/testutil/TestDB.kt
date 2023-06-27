package no.nav.syfo.testutil

import io.mockk.every
import io.mockk.mockk
import java.sql.Connection
import java.sql.Timestamp
import no.nav.syfo.Environment
import no.nav.syfo.aktivermelding.db.PlanlagtMeldingDbModel
import no.nav.syfo.application.db.Database
import no.nav.syfo.application.db.DatabaseInterface
import org.testcontainers.containers.PostgreSQLContainer

class PsqlContainer : PostgreSQLContainer<PsqlContainer>("postgres:12")

class TestDB private constructor() {
    companion object {
        var database: DatabaseInterface
        val env = mockk<Environment>()
        val psqlContainer: PsqlContainer =
            PsqlContainer()
                .withExposedPorts(5432)
                .withUsername("username")
                .withPassword("password")
                .withDatabaseName("database")
                .withInitScript("db/dbinit-test.sql")

        init {
            psqlContainer.start()
            every { env.databaseUsername } returns "username"
            every { env.databasePassword } returns "password"
            every { env.dbName } returns "database"
            every { env.cloudSqlInstance } returns "instance"
            every { env.jdbcUrl() } returns psqlContainer.jdbcUrl
            try {
                database = Database(env, testDb = true)
            } catch (e: Exception) {
                database = Database(env, testDb = true)
            }
        }
    }
}

fun Connection.setUp() {
    use { connection ->
        connection
            .prepareStatement(
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
            )
            .executeUpdate()
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
    use { connection ->
        connection
            .prepareStatement(
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
            )
            .use {
                it.setObject(1, planlagtMeldingDbModel.id)
                it.setString(2, planlagtMeldingDbModel.fnr)
                it.setObject(3, planlagtMeldingDbModel.startdato)
                it.setString(4, planlagtMeldingDbModel.type)
                it.setTimestamp(5, Timestamp.from(planlagtMeldingDbModel.opprettet.toInstant()))
                it.setTimestamp(6, Timestamp.from(planlagtMeldingDbModel.sendes.toInstant()))
                it.setTimestamp(
                    7,
                    if (planlagtMeldingDbModel.avbrutt != null) {
                        Timestamp.from(planlagtMeldingDbModel.avbrutt?.toInstant())
                    } else {
                        null
                    }
                )
                it.setTimestamp(
                    8,
                    if (planlagtMeldingDbModel.sendt != null) {
                        Timestamp.from(planlagtMeldingDbModel.sendt?.toInstant())
                    } else {
                        null
                    }
                )
                it.execute()
            }
        connection.commit()
    }
}
