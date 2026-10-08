plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.10.3"
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

/*
 * === Temporary security overrides ===
 * Override Spring portfolio to address vulnerabilities reported by Grype.
 * TODO: Remove these overrides once Spring Boot 4.1.2 OR 4.2.0 is released
 */
extra["tomcat.version"] = "11.0.26"
extra["jackson-bom.version"] = "3.2.3"
extra["jacksonAnnotationsVersion"] = "2.22"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    compileOnly("org.projectlombok:lombok")
    implementation("io.vavr:vavr:1.0.1")
    implementation("io.vavr:vavr-jackson:1.0.0")
    annotationProcessor("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

dependencyManagement {
    /*
     * === Temporary Jackson compatibility override ===
     * Spring Boot 4.1.1 pins Jackson Annotations 2.21, but Jackson BOM 3.2.3 requires 2.22.
     * TODO: Remove after upgrading Spring Boot when dependencyInsight resolves jackson-annotations
     * to 2.22 or later without this override.
     */
    dependencies {
        dependency("com.fasterxml.jackson.core:jackson-annotations:${property("jacksonAnnotationsVersion")}")
    }
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

tasks.withType<Test> {
    useJUnitPlatform()
}
