plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.0"
    java
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
}

tasks.withType<Test> {
    useJUnitPlatform()
}

