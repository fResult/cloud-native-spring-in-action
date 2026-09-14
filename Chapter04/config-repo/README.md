# Config Repo

This directory contains the **configuration repository** used by the Spring Cloud Config Server in Chapter 4.

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

For a high-level overview of Chapter 4 and links to other sections, see:

- [`../README.md`](../README.md)

