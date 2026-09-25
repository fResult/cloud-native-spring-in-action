plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.10.1"
}

group = "com.polarbookshop"
version = "0.0.1-SNAPSHOT"
description = "Centralizes the application configuration"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

repositories {
    mavenCentral()
}

/*
 * === Temporary security overrides ===
 * Override Spring portfolio to address vulnerabilities reported by Grype.
 * TODO: Remove these overrides once Spring Boot 4.1.2 OR 4.2.0 is released
 */
extra["tomcat.version"] = "11.0.25"

// === Explicit BOM selection ===
extra["springCloudVersion"] = "2025.1.3"

dependencies {
    implementation("org.springframework.cloud:spring-cloud-config-server")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

spotless {
    java {
        palantirJavaFormat()
            .style("GOOGLE")
            .formatJavadoc(true)

        importOrder()
        removeUnusedImports()

        target("**/*.java")
        targetExclude("**/build/**")
    }

    kotlinGradle {
        ktlint()
        target("*.gradle.kts")
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
