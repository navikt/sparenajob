import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "no.nav.syfo"
version = "1.0.0"

val coroutinesVersion = "1.8.1"
val jacksonVersion = "2.17.2"
val kluentVersion = "1.73"
val ktorVersion = "2.3.12"
val logbackVersion = "1.5.7"
val logstashEncoderVersion = "8.0"
val prometheusVersion = "0.16.0"
val mockkVersion = "1.13.12"
val testContainerVersion = "1.20.1"
val postgresVersion = "42.7.4"
val flywayVersion = "10.17.3"
val hikariVersion = "5.1.0"
val googlePostgresVersion = "1.20.1"
val kotlinVersion = "2.0.20"
val kotestVersion = "5.9.1"
val ktfmtVersion = "0.44"
val snappyJavaVersion = "1.1.10.6"
val kafkaVersion = "3.8.0"
val commonsCompressVersion = "1.27.1"
val jvmVersion = JvmTarget.JVM_21


plugins {
    id("application")
    id("com.diffplug.spotless") version "6.25.0"
    kotlin("jvm") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.0"
}

application {
    mainClass.set("no.nav.syfo.BootstrapKt")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

kotlin {
    compilerOptions {
        jvmTarget = jvmVersion
    }
}


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$coroutinesVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    implementation("org.apache.kafka:kafka_2.12:$kafkaVersion")
    constraints {
        implementation("org.xerial.snappy:snappy-java:$snappyJavaVersion") {
            because("override transient from org.apache.kafka:kafka_2.12")
        }
    }

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    compileOnly("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("com.google.cloud.sql:postgres-socket-factory:$googlePostgresVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    testImplementation("org.testcontainers:postgresql:$testContainerVersion")
    constraints {
        testImplementation("org.apache.commons:commons-compress:$commonsCompressVersion") {
            because("overrides vulnerable dependency from org.testcontainers:postgresql")
        }
    }

    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.testcontainers:kafka:$testContainerVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty")
    }
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
}

tasks {

    shadowJar {
        mergeServiceFiles {
            setPath("META-INF/services/org.flywaydb.core.extensibility.Plugin")
        }
        archiveBaseName.set("app")
        archiveClassifier.set("")
        isZip64 = true
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to "no.nav.syfo.BootstrapKt",
                ),
            )
        }
    }

    test {
        useJUnitPlatform {}
        testLogging.showStandardStreams = true
    }

    spotless {
        kotlin { ktfmt(ktfmtVersion).kotlinlangStyle() }
        check {
            dependsOn("spotlessApply")
        }
    }
}
