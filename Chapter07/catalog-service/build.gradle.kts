import org.springframework.boot.buildpack.platform.build.PullPolicy
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.10.1"
}

group = "com.polarbookshop"
version = "0.0.1-SNAPSHOT"
description = "Provides functionality for managing the books in the catalog."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

repositories {
    mavenCentral()
}

configurations {
    compileOnly {
        extendsFrom(annotationProcessor.get())
    }
}

/*
 * === Temporary security overrides ===
 * Override Spring portfolio to address vulnerabilities reported by Grype.
 * TODO: Remove these overrides once Spring Boot 4.1.2 OR 4.2.0 is released
 */
extra["tomcat.version"] = "11.0.25"
extra["jackson-bom.version"] = "3.1.7"

// === Explicit BOM selection ===
extra["springCloudVersion"] = "2025.1.3"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.springframework.retry:spring-retry")
    compileOnly("org.projectlombok:lombok")
    implementation("io.vavr:vavr:1.0.1")
    implementation("io.vavr:vavr-jackson:1.0.0")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
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

tasks.withType<BootBuildImage> {
    imageName = "${projectDir.name}:${project.version}"
    val tiltDev = providers.gradleProperty("tiltDev").isPresent
    // The multi-architecture Jammy buildpack currently ships JRE 25 and 27, not 26.
    val jvmVersion = if (tiltDev) "27" else "${java.toolchain.languageVersion.get()}"
    if (tiltDev) {
        builder = "paketobuildpacks/builder-jammy-buildpackless-tiny"
        buildpacks = listOf("docker://paketobuildpacks/java:latest")
        runImage = "catalog-service-tilt-run:local"
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

tasks.withType<BootRun> {
    systemProperty("spring.profiles.active", "testdata")
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Tests use Testcontainers for their dependencies and must not require a local Config Server.
    systemProperty("spring.cloud.config.enabled", "false")
}
