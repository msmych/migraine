plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"

    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.github.pengrad:java-telegram-bot-api:${project.property("telegramBotApiVersion")}")
}

application {
    mainClass.set("bot.AppKt")
}
