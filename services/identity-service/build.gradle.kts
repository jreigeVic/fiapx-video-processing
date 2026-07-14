import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.java
import org.gradle.kotlin.dsl.repositories

plugins {
    java
    id("org.springframework.boot") version "3.3.2"
}

group = "com.fiapx"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.flywaydb:flyway-core")
    implementation("org.postgresql:postgresql")

    // JWT libraries - traceability: required by Identity Service for token handling (ADR-011, HLD 11)
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.testcontainers:junit-jupiter:1.18.0")
    testImplementation("com.h2database:h2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

