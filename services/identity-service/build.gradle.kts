import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.jacoco
import org.gradle.kotlin.dsl.java
import org.gradle.kotlin.dsl.repositories

plugins {
    java
    jacoco
    checkstyle
    pmd
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.sonarqube") version "4.4.1.3373"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("com.diffplug.spotless") version "6.25.0"
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
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.postgresql:postgresql")

    // JWT libraries - traceability: required by Identity Service for token handling (ADR-011, HLD 11)
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.testcontainers:junit-jupiter:1.18.0")
    testImplementation("org.testcontainers:postgresql:1.18.0")
    testImplementation("com.h2database:h2")

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

// Formatting gate (Spotless). AOSP variant of google-java-format keeps the
// 4-space indentation already used across the codebase, minimizing diff
// churn versus the 2-space Google default. Wired into `check` by default.
spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.22.0").aosp()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// Convention gate (Checkstyle): imports, naming, basic hygiene. Ruleset at
// config/checkstyle/checkstyle.xml. Wired into `check` by default.
checkstyle {
    toolVersion = "10.17.0"
    maxWarnings = 0
}

// Complexity/dead-code/bug-pattern gate (PMD). Deliberately excludes PMD's
// `documentation` category (would demand Javadoc, conflicting with this
// project's no-comments-unless-needed convention) and `codestyle` category
// (already covered by Spotless/Checkstyle) to avoid triple-reporting the
// same finding. Wired into `check` by default.
pmd {
    toolVersion = "7.0.0"
    ruleSets = listOf()
    ruleSetFiles = files("config/pmd/pmd-ruleset.xml")
}

// Test sources use a lighter ruleset (config/pmd/pmd-test-ruleset.xml) -
// see that file's <description> for why.
tasks.named<Pmd>("pmdTest") {
    ruleSetFiles = files("config/pmd/pmd-test-ruleset.xml")
}

