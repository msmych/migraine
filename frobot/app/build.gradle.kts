import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Matchers
import org.jooq.meta.jaxb.MatchersTableType
import java.io.IOException
import java.net.Socket

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    id("com.palantir.docker-run") version "0.34.0"
    id("org.flywaydb.flyway") version "9.8.1"
    id("nu.studer.jooq") version "8.0"
    application
}

repositories {
    mavenCentral()
}

val kotlinxSerializationVersion: String by project
val postgresVersion: String by project
val hikariCpVersion: String by project
val telegramBotApiVersion: String by project
val jupiterVersion: String by project
val mockkVersion: String by project
val assertJVersion: String by project

val frobotDbUser = System.getenv("FROBOT_DB_USER")
val frobotDbPassword = System.getenv("FROBOT_DB_PASSWORD")
val frobotDbName = System.getenv("FROBOT_DB_NAME")
val frobotDbPort = System.getenv("FROBOT_DB_PORT").toInt()
val frobotDbUrl = "jdbc:postgresql://localhost:$frobotDbPort/$frobotDbName"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    jooqGenerator("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariCpVersion")
    implementation("com.github.pengrad:java-telegram-bot-api:$telegramBotApiVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
}

dockerRun {
    name = "frobot-pg"
    image = "postgres:latest"
    ports("$frobotDbPort:5432")
    daemonize = true
    env(mapOf("POSTGRES_USER" to frobotDbUser, "POSTGRES_PASSWORD" to frobotDbPassword, "POSTGRES_DB" to frobotDbName))
}

tasks.dockerRun {
    onlyIf {
        !portIsInUse(frobotDbPort)
    }
}

flyway {
    url = frobotDbUrl
    user = frobotDbUser
    password = frobotDbPassword
    schemas = arrayOf("public")
    locations = arrayOf("filesystem:${projectDir.absolutePath}/src/main/resources/db/migration")
}

tasks.flywayMigrate {
    dependsOn("dockerRun")
    doFirst {
        Thread.sleep(2000)
    }
}

jooq {
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = frobotDbUrl
                    user = frobotDbUser
                    password = frobotDbPassword
                }
                generator.apply {
                    database.apply {
                        inputSchema = "public"
                        forcedTypes.add(
                            ForcedType().apply {
                                userType = "java.time.Instant"
                                converter = "uk.matvey.persistence.InstantConverter"
                                includeExpression = ".*.CREATED_AT|.*.UPDATED_AT"
                            }
                        )
                    }
                    strategy.apply {
                        matchers = Matchers().withTables(
                            MatchersTableType()
                                .withRecordImplements("uk.matvey.persistence.AuditedEntityRecord<UUID, FrobotRecord>")
                                .withExpression("frobot")
                        )
                    }
                    generate.apply {
                        isFluentSetters = true
                        isJavaTimeTypes = false
                    }
                }
            }
        }
    }
}

tasks.getByName("generateJooq") {
    dependsOn("flywayMigrate")
}

tasks.dockerStop {
    onlyIf {
        portIsInUse(frobotDbPort)
    }
}

tasks.dockerRemoveContainer {
    dependsOn("dockerStop")
}

tasks.clean {
    dependsOn("dockerRemoveContainer")
}

fun portIsInUse(port: Int) = try {
    Socket("localhost", port).close()
    true
} catch (e: IOException) {
    false
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("uk.matvey.frobot.AppKt")
}
