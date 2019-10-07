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
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.eclipse.jetty:jetty-server:9.4.21.v20190926")
    implementation("org.eclipse.jetty:jetty-servlet:9.4.21.v20190926")
    // json support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.7")
    // json to xml support
    implementation("com.github.javadev:underscore:1.48")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}