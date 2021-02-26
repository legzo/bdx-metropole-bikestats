import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val http4kVersion = "4.3.5.4"

plugins {
    kotlin("jvm") version "1.4.30"
    application
}

group = "com.jtelabs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.slf4j:slf4j-api") { version { strictly("1.7.30") }   }
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("org.http4k:http4k-client-okhttp:${http4kVersion}")
    implementation("org.http4k:http4k-core:${http4kVersion}")
    implementation("org.http4k:http4k-format-jackson:${http4kVersion}")
    implementation("org.http4k:http4k-opentelemetry:${http4kVersion}")
    implementation("org.http4k:http4k-server-undertow:${http4kVersion}")
    implementation("org.http4k:http4k-cloudnative:${http4kVersion}")

    testImplementation("org.http4k:http4k-testing-approval:${http4kVersion}")
    testImplementation("org.http4k:http4k-testing-hamkrest:${http4kVersion}")
    testImplementation("org.http4k:http4k-testing-kotest:${http4kVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks.register("stage") {
    dependsOn("installDist")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes")
}

application {
    mainClassName = "org.jtelabs.bikestats.AppKt"
}