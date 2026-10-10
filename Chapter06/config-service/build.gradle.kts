import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.10.3"
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
extra["jacksonAnnotationsVersion"] = "2.22"
extra["tomcat.version"] = "11.0.26"
extra["jackson-bom.version"] = "3.2.3"

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

        target("**/*.java")
        targetExclude("**/build/**")

        importOrder()
        removeUnusedImports()
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }

    kotlinGradle {
        ktlint()
        target("*.gradle.kts", "settings.gradle.kts")
    }
}

dependencyManagement {
    dependencies {
        dependency("com.fasterxml.jackson.core:jackson-annotations:${property("jacksonAnnotationsVersion")}")
    }

    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<BootBuildImage> {
    imageName = "${projectDir.name}:${project.version}"
    environment = mapOf("BP_JVM_VERSION" to "${java.toolchain.languageVersion.get()}")

    docker {
        publishRegistry {
            username = providers.gradleProperty("registryUsername")
            password = providers.gradleProperty("registryToken")
            url = providers.gradleProperty("registryUrl")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
