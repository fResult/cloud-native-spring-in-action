# Config Service – Centralized Configuration Management with Spring Cloud Config Server

Configuration data is stored in a separate Git repository and served to clients via a REST API.

This service is the **Spring Cloud Config Server** for Chapter 4.\
It provides centralized configuration to other applications (for example, `catalog-service`) using a Git-backed config repository.

Configuration data is stored in:

- Remote repo: [`cloud-native-spring-config-repo`](https://github.com/fResult/cloud-native-spring-config-repo)
- Local mirror (for reference in this chapter): [`../config-repo`](../config-repo)

## 1. Running Config Service

From the `config-service` directory:

```console
→ ./gradlew bootRun
```

The server starts on port `8888`.

## 2. How Config Service Reads Configuration

Config Service is configured (via `application.yml`) to:

- Run on port **8888**
- Use `spring.application.name=config-service`
- Use the remote Git repo `cloud-native-spring-config-repo` as its backend
- Clone the repo locally on startup and keep it in sync (`timeout`, `clone-on-start`, `force-pull`)

Config files in the Git repo follow the Spring Cloud Config naming conventions, for example:

- `catalog-service.yml`
- `catalog-service-prod.yml`

These are resolved based on:

- `{application}` - e.g. `catalog-service`
- `{profile}` - e.g. `default`, `prod`
- `{label}` - Git label (branch/tag/commit), defaulting to `main`

## 3. Exploring the Config Server REST API

Spring Cloud Config Server exposes configuration via a REST API.  
You can query it directly with HTTPie to see what configuration will be served to clients.

### 3.1 `catalog-service` with default profile

```console
→ http :8888/catalog-service/default  
{
  "label": null,
  "name": "catalog-service",
  "profiles": [
    "default"
  ],
  "propertySources": [
    {
      "name": "https://github.com/fResult/cloud-native-spring-config-repo/catalog-service.yml",  
      "source": {
        "polar.greeting": "Welcome to the catalog from the config server"
      }
    }
  ],
  "state": "",
  "version": "9a5fc112355c1741fb02a6a31ebe4a43ef60c2b9"  
}
```

### 3.2 `catalog-service` with `prod` profile

```console
→ http :8888/catalog-service/prod
{
  "label": null,
  "name": "catalog-service",
  "profiles": [
    "prod"
  ],
  "propertySources": [
    {
      "name": "https://github.com/fResult/cloud-native-spring-config-repo/catalog-service-prod.yml",
      "source": {
        "polar.greeting": "Welcome to the production catalog from the config server!"
      }
    },
    {
      "name": "https://github.com/fResult/cloud-native-spring-config-repo/catalog-service.yml",
      "source": {
        "polar.greeting": "Welcome to the catalog from the config server"
      }
    }
  ],
  "state": "",
  "version": "9a5fc112355c1741fb02a6a31ebe4a43ef60c2b9"
}
```

## 4. Relationship to Other Components

- **Config Repo**  
  Git-backed configuration data for all services
  - Remote: [`cloud-native-spring-config-repo`](https://github.com/fResult/cloud-native-spring-config-repo)
  - Local mirror: [`../config-repo`](../config-repo)
- **Catalog Service (Config Client)**  
  Will be configured to use Spring Cloud Config Client and read `polar.greeting` (and other properties) from this Config Service.\  
  See: [`../catalog-service/README.md`](../catalog-service/README.md)

## 5. Using Config Service with Catalog Service (Section 4.4.1)

In Section 4.4.1, `catalog-service` is configured as a Spring Cloud Config Client that talks to this Config Service instance:

1. Start Config Service:
   ```console
   → ./gradlew bootRun
   ```
2. Ensure `config-repo` is available (see `../config-repo/README.md`).
3. Follow the steps in `../catalog-service/README.md` to run Catalog Service and verify that:
   - `http :9001/` returns the greeting from `catalog-service.yml`
   - `http :9001/` with `--spring.profiles.active=prod` returns the greeting from `catalog-service-prod.yml`

---

> For a high-level overview of Chapter 4 and links to other sections, see:  
> [`../README.md`](../README.md)
