
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
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

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jasync.postgresql)
    implementation(libs.logback.classic)
    implementation(libs.kotlin.logging)
    implementation(libs.flyway.core)
    implementation(libs.telek)

    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.flyway.database.postgresql)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.assertj.core)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
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
