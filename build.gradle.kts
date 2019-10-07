import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
}

group = "co.cy.students"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val http4kVersion = "3.188.0"
    val junitVersion = "5.4.2"

    implementation(kotlin("stdlib-jdk8"))
    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-server-netty:$http4kVersion")
    implementation("org.http4k:http4k-client-okhttp:$http4kVersion")
    implementation("org.http4k:http4k-cloudnative:$http4kVersion")
    implementation("org.http4k:http4k-contract:$http4kVersion")
    implementation("org.http4k:http4k-format-jackson:$http4kVersion")
    implementation("org.http4k:http4k-format-jackson-xml:$http4kVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.7")

    implementation("org.http4k:http4k-security-oauth:$http4kVersion")
    implementation("org.http4k:http4k-server-undertow:$http4kVersion")


    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.http4k:http4k-testing-hamkrest:$http4kVersion")
    testImplementation("org.http4k:http4k-testing-chaos:$http4kVersion")
    testImplementation("org.http4k:http4k-testing-approval:$http4kVersion")
    testImplementation("org.http4k:http4k-testing-webdriver:$http4kVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events = setOf(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_ERROR,
            TestLogEvent.STANDARD_OUT,
            TestLogEvent.STARTED
        )
    }
}