plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.10"

    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.pengrad:java-telegram-bot-api:6.6.1")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest("1.7.10")

            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
            }
        }
    }
}

application {
    mainClass.set("frobot.AppKt")
}
