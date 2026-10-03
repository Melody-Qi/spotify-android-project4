val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val kotlin_serialization: String by project

plugins {
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
    application
}

group = "com.laioffer"
version = "0.0.1"

application {
    mainClass.set("com.laioffer.ApplicationKt")
}

kotlin {
    // NOTE (2026-10-04): was jvmToolchain(17). No JDK 17 is installed any more
    // (only 21 at D:/Java/jdk-21, plus JDK 24 and the IDE's JBR 25), so Gradle
    // failed with "No matching toolchains found for requested specification:
    // {languageVersion=17...}". Raised to the JDK we actually have.
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlin_serialization")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
