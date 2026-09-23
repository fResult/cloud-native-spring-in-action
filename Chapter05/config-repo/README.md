# Config Repo

This directory contains the **configuration repository** used by the Spring Cloud Config Server in Chapter 5.

In the book, this corresponds to section **4.3.1 – Using Git to store your configuration data**:  
configuration data is stored in a **separate Git repository**, and the Config Server reads from that repo to serve configuration to client applications.

> [!IMPORTANT]
> This `config-repo/` folder is a **local mirror** kept in the same Git repository as the code for convenience.  
> The **actual** remote config repository used by the Config Server is:
>
> - [`cloud-native-spring-config-repo`](https://github.com/fResult/cloud-native-spring-config-repo)

## Purpose

- Act as the **Git-backed configuration store** for the Config Service (Spring Cloud Config Server)
- Provide external configuration for:
  - `catalog-service` (Catalog Service application in this chapter)
- Keep the config files close to the code so they are easy to inspect while reading the book notes

This repo is consumed by the **Config Service** project:

- [`../config-service`](../config-service)

## Catalog Service configuration files

For `catalog-service`, the main configuration files are:

- `catalog-service.yml` – default configuration (e.g. `polar.greeting: "Welcome to the catalog from the config server!"`)
- `catalog-service-prod.yml` – production-specific configuration (e.g. `polar.greeting: "Welcome to the production catalog from the config server"`)

These files are selected by the Config Service based on:
- `spring.application.name=catalog-service`
- `spring.profiles.active` (e.g. `prod`)

They are used in:
- Section 4.4.1 – to override local configuration in Catalog Service.
- Section 4.4.3 – to demonstrate changing configuration at runtime:
  - Update `polar.greeting` in `catalog-service.yml`.
  - Commit and push the change.
  - Trigger a refresh in Catalog Service via `/actuator/refresh`.

---

For a high-level overview of Chapter 4 and links to other sections, see:

- [`../README.md`](../README.md)
