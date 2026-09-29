plugins {
    kotlin("jvm") version "1.9.24"
    application
}

group = "com.laioffer"
version = "0.0.1"

application {
    mainClass.set("com.laioffer.MainKt")
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}
