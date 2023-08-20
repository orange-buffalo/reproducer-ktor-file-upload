plugins {
    kotlin("jvm") version "1.9.0"
}

group = "io.orange-buffalo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktor_version = "2.3.3"

dependencies {
    testImplementation(kotlin("test"))

    testImplementation("io.ktor:ktor-client-core:$ktor_version")
    testImplementation("io.ktor:ktor-client-cio:$ktor_version")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    testImplementation("com.github.tomakehurst:wiremock:2.27.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}
