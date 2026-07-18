plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.sonarqube") version "4.4.1.3373"
    id("com.github.ben-manes.versions") version "0.51.0"
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

// Fixed platform choice (ADR-010: SonarQube Cloud), not environment-specific
// configuration: every invocation of the sonar task targets SonarCloud,
// never a local SonarQube server. Token/organization/project key remain
// CI-supplied (see ci.yml) since those vary per environment.
sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.flywaydb:flyway-core")

    // AWS SDK v2 BOM and modules - traceability: required by Processing Worker for S3/SNS/SQS (ADR-011, LLD)
    implementation(platform("software.amazon.awssdk:bom:2.20.0"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:sns")
    implementation("software.amazon.awssdk:sqs")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.testcontainers:junit-jupiter:1.18.0")

    // ArchUnit - traceability: enforces Hexagonal Architecture boundaries in CI (see src/test/.../architecture)
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Report-only dependency freshness check (com.github.ben-manes.versions).
// Never fails the build and never updates anything automatically - see
// pendencies.md / docs/HLD "Dependencias" guidance: report, don't auto-upgrade.
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates").configure {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

