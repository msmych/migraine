import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Matchers
import org.jooq.meta.jaxb.MatchersTableType
import java.io.IOException
import java.net.Socket

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("com.palantir.docker-run") version "0.34.0"
    id("org.flywaydb.flyway") version "9.8.1"
    id("nu.studer.jooq") version "8.0"
    id("com.gradleup.shadow") version "8.3.6"
    application
}

repositories {
    mavenCentral()
    maven {
        name = "TelekPackages"
        url = uri("https://maven.pkg.github.com/msmych/telek")
        credentials {
            username = "migraine"
            password = project.findProperty("ghPackagesRoToken") as? String ?: System.getenv("GH_PACKAGES_RO_TOKEN")
        }
    }
}

val kotlinxSerializationVersion: String by project
val postgresVersion: String by project
val hikariCpVersion: String by project
val logbackVersion: String by project
val kotlinLoggingVersion: String by project
val jupiterVersion: String by project
val mockkVersion: String by project
val assertJVersion: String by project

dependencies {
    implementation("io.ktor:ktor-server-core:3.2.0")
    implementation("io.ktor:ktor-server-netty:3.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    jooqGenerator("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariCpVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("org.flywaydb:flyway-core:11.0.1")
    implementation("uk.matvey:telek:0.1.0-RC13")

    runtimeOnly("org.flywaydb:flyway-database-postgresql:11.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dockerRun {
    name = "frobot-pg"
    image = "postgres:latest"
    ports("55000:5432")
    daemonize = true
    env(mapOf("POSTGRES_USER" to "postgres", "POSTGRES_PASSWORD" to "postgres", "POSTGRES_DB" to "postgres"))
}

tasks.dockerRun {
    onlyIf {
        !portIsInUse(55000)
    }
}

flyway {
    url = "jdbc:postgresql://localhost:55000/postgres"
    user = "postgres"
    password = "postgres"
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
                    url = "jdbc:postgresql://localhost:55000/postgres"
                    user = "postgres"
                    password = "postgres"
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
        portIsInUse(55000)
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

tasks.shadowJar {
    transform(ServiceFileTransformer::class.java)
}
