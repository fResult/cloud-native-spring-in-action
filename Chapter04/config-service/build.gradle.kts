plugins {
  java
  id("org.springframework.boot") version "4.1.1"
  id("io.spring.dependency-management") version "1.1.7"
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

extra["springCloudVersion"] = "2025.1.3"

dependencies {
  implementation("org.springframework.cloud:spring-cloud-config-server")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
