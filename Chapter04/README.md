# Chapter 4 - Externalized Configuration Management

This chapter focuses on managing application configuration externally in cloud native Spring applications.

You start by:
- Understanding **properties** and **profiles** in Spring (4.1)
- Learning how to externalize configuration using **command-line arguments**, **JVM system properties**, and **environment variables** (4.2)

Then you:
- Build a **centralized configuration management** solution using **Spring Cloud Config Server** with Git as the configuration data store (4.3)
- Configure applications as **Spring Cloud Config Clients**, make them resilient, and **refresh configuration at runtime** (4.4)

> [!NOTE]
> The notes in this repository currently cover section **4.2 – Externalized configuration: One build, multiple configurations**.  \
> Sections **4.1, 4.3, and 4.4** are not yet documented here.

For reference, the official source code for this chapter is available at:

> [ThomasVitale/cloud-native-spring-in-action/Chapter04/04-end](https://github.com/ThomasVitale/cloud-native-spring-in-action/tree/main/Chapter04/04-end)

## 1. Prerequisites

To follow the hands-on exercises for this chapter:

- **Java 25+**
- [Docker](https://docs.docker.com/engine/install)
- [Kubernetes](https://kubernetes.io/releases/download)
- [**Grype**](https://oss.anchore.com/docs/installation/grype) – a powerful vulnerability scanner
- [HTTPie](https://httpie.io/cli)

## 2. Project Structure

This chapter’s code in this repo is organized as:

- `Chapter04/` – high-level notes and utilities for Chapter 4
  - `catalog-service/` – Catalog Service application used to demonstrate **externalized configuration** (section 4.2)
    - `README.md` – step-by-step guide for:
      - Command-line arguments
      - JVM system properties
      - Environment variables
    - `src/main/...` – Spring Boot application code
    - `application.yml` – default configuration properties

## 3. Development Scripts

### 3.1 Building the Project

From the `catalog-service` module, build the project:

```console
→ ./gradlew build
```

This generates the executable JAR artifact, e.g.:

```console
build/libs/catalog-service-0.0.1-SNAPSHOT.jar
```

### 3.2 Running Grype (Vulnerability Scan)

After building the project, from the project root directory, scan for vulnerabilities:

```console
→ grype .
 ✔ Vulnerability DB                [updated]  
 ✔ Indexed file system                                                                                                    .
 ✔ Cataloged contents
   ├── ✔ Packages                        [0 packages]  
   └── ✔ Executables                     [0 executables]  
 ✔ Scanned for vulnerabilities     [0 vulnerability matches]  
   ├── by severity: 0 critical, 0 high, 0 medium, 0 low, 0 negligible
   └── by status:   0 fixed, 0 not-fixed, 0 ignored
No vulnerabilities found
```

The output will list any discovered vulnerabilities (if any).

### 3.3 Testing the REST API (Catalog Service)

Start the Catalog Service (for example):

```console
→ ./gradlew bootRun
```

Then test the `/books` endpoint:

```console
→ http :9001/books
HTTP/1.1 200
Content-Type: application/json

[
  {
    "author": "Lyra Silverstar",
    "isbn": "1234567891",
    "price": 9.9,
    "title": "Northern Lights"
  },
  {
    "author": "Iorek Polason",
    "isbn": "1234567892",
    "price": 12.9,
    "title": "Polar Journey"
  }
]
```

For more detailed API and configuration examples, see:

> [`catalog-service/README.md`](catalog-service/README.md)

## 4. Chapter Overview (Mapping to TOC)

This section maps the book's Table of Contents for Chapter 4 to the hands-on notes in this repo.

### 4.1 Configuration in Spring: Properties and Profiles

Concepts (not yet fully documented here):

- **Properties** – key/value pairs for configuration (e.g. `application.yml`, `application.properties`)
- **Profiles** – feature flags / configuration groups (e.g. `spring.profiles.active=dev`)

Typical topics:

- How Spring loads properties into the `Environment`
- How to define different profiles (`dev`, `test`, `prod`)
- How to activate profiles via CLI, environment variables, or config server

> TODO: Add hands-on examples for properties and profiles (section 4.1).

### 4.2 Externalized Configuration: One Build, Multiple Configurations

Goal: Use **one immutable build** (one JAR) and change configuration depending on the environment.

Covered in this repo via `catalog-service/README.md`:

- Configuring an application through **command-line arguments**
- Configuring an application through **JVM system properties**
- Configuring an application through **environment variables**

> For step-by-step commands and terminal examples, see:  
> [`catalog-service/README.md`](catalog-service/README.md)














