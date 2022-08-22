package no.nav.syfo

data class Environment(
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),
    val applicationName: String = getEnvVar("NAIS_APP_NAME", "sparenajob"),
    val aktiverMeldingTopic: String = "teamsykmelding.privat-aktiver-planlagtmelding",
    val databaseUsername: String = getEnvVar("NAIS_DATABASE_SPARENAJOB_USERNAME"),
    val databasePassword: String = getEnvVar("NAIS_DATABASE_SPARENAJOB_PASSWORD"),
    val dbHost: String = getEnvVar("NAIS_DATABASE_SPARENAJOB_HOST"),
    val dbPort: String = getEnvVar("NAIS_DATABASE_SPARENAJOB_PORT"),
    val dbName: String = getEnvVar("NAIS_DATABASE_SPARENAJOB_DATABASE"),
    val cloudSqlInstance: String = getEnvVar("CLOUD_SQL_INSTANCE")
) {
    fun jdbcUrl(): String {
        return "jdbc:postgresql://$dbHost:$dbPort/$dbName"
    }
}

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
