import org.springframework.boot.buildpack.platform.build.PullPolicy
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    java
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.11.0"
}

group = "com.polarbookshop"
version = "0.0.1-SNAPSHOT"
description = "Centralizes the application configuration"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// === Explicit BOM selection ===
extra["springCloudVersion"] = "2025.1.4"

dependencies {
    implementation("org.springframework.cloud:spring-cloud-config-server")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
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
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<BootBuildImage> {
    imageName = "${projectDir.name}:${project.version}"
    val tiltDev = providers.gradleProperty("tiltDev").isPresent
    val jvmVersion =
        java.toolchain.languageVersion
            .get()
            .toString()
    if (tiltDev) {
        builder = "paketobuildpacks/builder-jammy-buildpackless-tiny"
        buildpacks = listOf("docker://paketobuildpacks/java:latest")
        runImage = "config-service-tilt-run:local"
        // Keep the locally built dev run image but pull missing builder/buildpack images.
        pullPolicy = PullPolicy.IF_NOT_PRESENT
    }
    environment =
        mapOf(
            "BP_JVM_VERSION" to jvmVersion,
            "BP_LIVE_RELOAD_ENABLED" to if (tiltDev) "true" else "false",
        )

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
