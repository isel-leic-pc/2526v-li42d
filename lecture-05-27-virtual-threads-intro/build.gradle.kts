plugins {
    kotlin("jvm") version "2.3.0"
}

group = "pt.isel.pc"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    // logging
    runtimeOnly("org.slf4j:slf4j-simple:2.0.16")
    implementation("io.github.microutils:kotlin-logging:3.0.5")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}