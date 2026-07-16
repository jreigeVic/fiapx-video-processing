plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.sonarqube") version "4.4.1.3373"
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
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.flywaydb:flyway-core")
    implementation("org.postgresql:postgresql")

    // AWS SDK v2 BOM and S3 module - traceability: required by Video Service for S3 storage (ADR-011, LLD)
    implementation(platform("software.amazon.awssdk:bom:2.20.0"))
    implementation("software.amazon.awssdk:s3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.testcontainers:junit-jupiter:1.18.0")
    testImplementation("com.h2database:h2")
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

