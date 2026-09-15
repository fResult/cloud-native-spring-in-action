# Chapter 4 – Externalized Configuration Management

This chapter focuses on managing application configuration externally in cloud native Spring applications.

You start by:
- Understanding **properties** and **profiles** in Spring (4.1)
- Learning how to externalize configuration using **command-line arguments**, **JVM system properties**, and **environment variables** (4.2)

Then you:
- Build a **centralized configuration management** solution using **Spring Cloud Config Server** with Git as the configuration data store (4.3)
- Configure applications as **Spring Cloud Config Clients**, make them resilient, and **refresh configuration at runtime** (4.4)

> [!NOTE]
> The notes in this repository currently cover:
> - **4.2 – Externalized configuration: One build, multiple configurations**
> - **4.3 – Centralized configuration management with Spring Cloud Config Server** (selected parts)
> - **4.4 – Using a configuration server with Spring Cloud Config Client** (hands-on for Catalog Service)
>
> The coverage for 4.4 is focused on the main hands-on flow (client setup, resilience, and manual refresh).\
> More advanced production scenarios (e.g. Spring Cloud Bus, webhooks, secrets management) are not documented here.

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

- `catalog-service/` – Catalog Service application used to demonstrate:
  - Externalized configuration (Section 4.2)
  - Spring Cloud Config Client (Section 4.4.1)
  - Client resilience and retry (Section 4.4.2)
  - Runtime configuration refresh (Section 4.4.3)
  - `README.md` – step-by-step guide for:
    - Command-line arguments
    - JVM system properties
    - Environment variables
    - Spring Cloud Config Client + resilience + refresh

## 3. Hands-on Modules in This Chapter

This chapter includes the following hands-on modules:

- `catalog-service/` – demonstrates **externalized configuration** using:
  - Command-line arguments
  - JVM system properties
  - Environment variables
  - See: [`catalog-service/README.md`](catalog-service/README.md)
- `config-repo/` – local mirror of the **Git-backed configuration repository** used by Spring Cloud Config Server.
  - See: [`config-repo/README.md`](config-repo/README.md)
- `config-service/` – **Spring Cloud Config Server** providing centralized configuration via REST.
  - See: [`config-service/README.md`](config-service/README.md)

## 4. Chapter Overview (Mapping to Table of Content)

This section maps the book's Table of Contents for Chapter 4 to the hands-on notes in this repo.

### 4.1 Configuration in Spring: Properties and Profiles

Concepts (not yet fully documented here):

- **Properties** – key/value pairs for configuration (e.g. `application.yml`, `application.properties`)
- **Profiles** – feature flags / configuration groups (e.g. `spring.profiles.active=dev`)

Typical topics:

- How Spring loads properties into the `Environment`
- How to define different profiles (`dev`, `test`, `prod`)
- How to activate profiles via CLI, environment variables, or config server

### 4.2 Externalized Configuration: One Build, Multiple Configurations

Goal: Use **one immutable JAR** and change configuration depending on the environment.

Hands-on in this repo:
- `catalog-service/` – shows how to override configuration via:
  - Command-line arguments
  - JVM system properties
  - Environment variables (recommended)
- Step-by-step examples:
  - Building the JAR once
  - Running with different property sources
  - Understanding property precedence

> For detailed commands and examples, see:  
> [`catalog-service/README.md`](catalog-service/README.md)

### 4.3 Centralized Configuration Management with Spring Cloud Config Server

Goal: Store configuration in **Git** and serve it centrally via a **Spring Cloud Config Server**.

Concepts:

- Using **Git** as the single source of truth for configuration data
- Exposing configuration via a **REST API**
- Resolving configuration by `{application}`, `{profile}`, `{label}`

Implemented in this repo as:

- `config-repo/` – local mirror of the Git-backed configuration repository
  - Mirrors: [`cloud-native-spring-config-repo`](https://github.com/fResult/cloud-native-spring-config-repo)
  - See: [`config-repo/README.md`](config-repo/README.md)
- `config-service/` – Spring Cloud Config Server
  - Reads from the Git repo and exposes configuration via HTTP (e.g. `/catalog-service/default`, `/catalog-service/prod`)
  - See: [`config-service/README.md`](config-service/README.md)

### 4.4 Using a Configuration Server with Spring Cloud Config Client

- Demonstrated in:
  - `config-service` (Config Server)
  - `config-repo` (centralized configuration files)
  - `catalog-service` (Config Client)

Hands-on coverage in this repo:

- **4.4.1 – Setting up a configuration client**
  - `catalog-service` uses:
    - `spring-cloud-starter-config`
    - `spring.application.name=catalog-service`
    - `spring.config.import=optional:configserver:`
    - `spring.cloud.config.uri=http://localhost:8888`
- **4.4.2 – Making the configuration client resilient**
  - `catalog-service` configures:
    - timeouts (`request-connect-timeout`, `request-read-timeout`)
    - optional retry behavior via Spring Retry (`spring.cloud.config.retry.*`, `fail-fast`)
- **4.4.3 – Refreshing configuration at runtime**
  - `catalog-service` uses Spring Boot Actuator:
    - exposes `/actuator/refresh`
    - reloads `@ConfigurationProperties` beans when a refresh is triggered
  - `config-repo` stores updated configuration (e.g. new `polar.greeting` values)
  - `config-service` serves the latest configuration from Git
