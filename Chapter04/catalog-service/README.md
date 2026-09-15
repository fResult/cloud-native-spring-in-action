# Catalog Service - Externalized Configuration & Config Client

This guide demonstrates:
- How to **externalize application configuration** using command-line arguments, JVM system properties, and environment variables (Section 4.2).
- How to **use Spring Cloud Config Client** to fetch centralized configuration from a Config Service (Section 4.4).

The examples are based on section **4.2 – Externalized configuration: One build, multiple configurations** of *Cloud Native Spring in Action*.

## 1. Building the Application (Immutable JAR)

First, package the application as a JAR artifact:

```console
→ ./gradlew bootJar
```

(or `./gradlew build` – both will produce the JAR)

The compiled JAR artifact is generated at:

```console
build/libs/catalog-service-0.0.1-SNAPSHOT.jar
```

The key idea: **you will keep using this same JAR** and only change configuration from the outside.

## 2. Externalized configuration with local property sources (Section 4.2)


### 2.1 Running with Default Configuration (`application.yml`)

Run the application as a standard Java application using the default configuration defined in `application.yml`:

```console
→ java -jar build/libs/catalog-service-0.0.1-SNAPSHOT.jar
```

In a separate terminal window, send an HTTP request to test the root endpoint:

```console
→ http :9001  
Welcome to the local book catalog!
```

At this point, you haven’t overridden any properties yet, so the root endpoint returns the `polar.greeting` value defined in `application.yml`.

> Remember to terminate the Java process with <kbd>CTRL</kbd>+<kbd>C</kbd> before running each new example.

### 2.2 Configuring via Command-Line Arguments (4.2.1)

By default, Spring Boot converts any **command-line argument** into a property key/value pair and includes it in the `Environment` object.\
In a production application, CLI arguments are one of the property sources with **highest precedence**.

Using the same JAR you built earlier, you can specify a command-line argument to customize the application configuration:

```console
→ java -jar build/libs/catalog-service-0.0.1-SNAPSHOT.jar \
  --polar.greeting="Welcome to the catalog from CLI"
```

The command-line argument has the same name as the Spring property, prefixed with `--`.

Now test the root endpoint:

```console
→ http :9001
Welcome to the catalog from CLI
```
The application uses the message defined in the command-line argument, since it takes precedence over property files.

### 2.3 Configuring via JVM System Properties (4.2.2)

JVM system properties can override Spring properties much like command-line arguments, but they have **lower priority** than CLI arguments.

Terminate the previous Java process (<kbd>CTRL</kbd>+<kbd>C</kbd>) and run:

```console
→ java -Dpolar.greeting="Welcome to the catalog from JVM" \
  -jar build/libs/catalog-service-0.0.1-SNAPSHOT.jar
```

Here, the JVM system property has the same name as the Spring property, prefixed with `-D`.

Test the root endpoint:

```console
→ http :9001
Welcome to the catalog from JVM
```

The application uses the message defined as a JVM system property, since it takes precedence over property files (`application.yml`).

### 2.4 Property Precedence: CLI vs JVM vs `application.yml`

What happens if you specify both a JVM system property and a CLI argument?

Terminate the previous Java process (<kbd>CTRL</kbd>+<kbd>C</kbd>) and run:

```console
→ java -Dpolar.greeting="Welcome to the catalog from JVM" \
  -jar build/libs/catalog-service-0.0.1-SNAPSHOT.jar \
  --polar.greeting="Welcome to the catalog from CLI"
```

Test the root endpoint:

```console
→ http :9001
Welcome to the catalog from CLI
```

Spring Boot's precedence rules ensure that the **command-line argument** value is used, since it has higher precedence than JVM properties and property files.

Summary of precedence (for this example):

1. Command-line arguments (`--polar.greeting=...`)
2. JVM system properties (`-Dpolar.greeting=...`)
3. Property files (`application.yml`)

### 2.5 Configuring via Environment Variables (4.2.3)

Environment variables are the recommended option according to the **15-Factor methodology**.\
They are:
- Supported by every operating system
- Portable across environments (VMs, containers, Kubernetes)
- Automatically read by Spring and added to the `Environment` object

Spring Boot extends Spring by allowing environment variables to **override Spring properties automatically** using **relaxed binding**:

- Make all letters uppercase
- Replace dots (`.`) and dashes (`-`) with underscores (`_`)

Example mapping:

- Environment variable: `POLAR_GREETING`
- Spring property: `polar.greeting`

#### 2.5.1 Overriding `polar.greeting` via Environment Variable (Linux/macOS)

Terminate any running process (<kbd>CTRL</kbd>+<kbd>C</kbd>), then run:

```console
→ POLAR_GREETING="Welcome to the catalog from ENV" \
  java -jar build/libs/catalog-service-0.0.1-SNAPSHOT.jar
```

During startup, Spring Boot:
1. Reads environment variables from the surrounding system
2. Recognizes that `POLAR_GREETING` maps to `polar.greeting`
3. Stores its value in the Spring `Environment` object
4. Overrides the value defined in `application.yml`

```console
→ http :9001
Welcome to the catalog from ENV
```

After testing, stop the process with <kbd>CTRL</kbd>+<kbd>C</kbd>.

#### 2.5.2 Overriding via Environment Variable (Windows PowerShell)

On Windows PowerShell, you can achieve the same result with:

```powershell
PS> $env:POLAR_GREETING="Welcome to the catalog from ENV"
PS> java -jar build/libs/catalog-service-0.0.1-SNAPSHOT.jar
```

Test the endpoint:

```console
→ http :9001
Welcome to the catalog from ENV
```

After testing, unset the environment variable:

```powershell
PS> Remove-Item Env:\POLAR_GREETING
```

#### 2.5.3 Why Environment Variables Are Preferred

When you use environment variables for storing configuration data:

- You **don’t have to change the command** used to run your application (unlike CLI args or JVM properties)
- Spring automatically reads environment variables from the deployment context
- The approach is **less error-prone** and more robust for cloud-native deployments

Typical configuration values that can be stored in environment variables:

- Profiles
- Port numbers
- IP addresses
- URLs
- Credentials (combined with a proper secrets management solution)

Environment variables work seamlessly on:

- Virtual machines
- OCI containers (Docker)
- Kubernetes clusters

> In later sections (4.3 and beyond), you'll see how to complement environment variables with centralized configuration services like **Spring Cloud Config**, and how to handle secrets and advanced configuration scenarios.

### 2.6 Recap (Section 4.2)

Using a single immutable JAR (`catalog-service-0.0.1-SNAPSHOT.jar`), you can externalize configuration by:

1. **Command-line arguments**\
   `--polar.greeting="Welcome to the catalog from CLI"`
2. **JVM system properties**\
   `-Dpolar.greeting="Welcome to the catalog from JVM"`
3. **Environment variables** (recommended)\
   `POLAR_GREETING="Welcome to the catalog from ENV"`

## 3. Using Spring Cloud Config Client (Section 4.4.1)

This section shows how to configure Catalog Service as a Spring Cloud Config Client that retrieves its configuration from [Config Service](../config-service/README.md) and [`config-repo`](../config-repo/README.md).

### 3.1 Enabling Spring Cloud Config Client

The `build.gradle.kts` file includes:

- `implementation("org.springframework.cloud:spring-cloud-starter-config")`
- The Spring Cloud BOM: `org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}`

This allows Catalog Service to act as a configuration client for Config Service.

### 3.2 Pointing Catalog Service to Config Service

The `application.yml` file configures Catalog Service as follows:

```yaml
spring:
  application:
    name: catalog-service
  config:
    import: "optional:configserver:"
  cloud:
    config:
      uri: http://localhost:8888
```

- `spring.application.name` – used by Config Service to select the right configuration file (e.g. `catalog-service.yml`).
- `spring.config.import=optional:configserver:` – imports configuration from the config server when available, but does not fail the application if the server is down (useful for local development).
- `spring.cloud.config.uri` – URL of the Config Service instance.

### 3.3 Running Catalog Service with Config Service

1. Start Config Service (see `../config-service/README.md`):
   ```console
   → ./gradlew bootRun
   ```
2. Ensure `config-repo` is available and contains:
   - `catalog-service.yml`
   - `catalog-service-prod.yml`
3. Build and run Catalog Service:
   ```console
   → ./gradlew bootJar
   → java -jar build/libs/catalog-service-0.0.1-SNAPSHOT.jar
   ```
4. Call the root endpoint:
   ```console
   → http :9001/
   Welcome to the catalog from the config server!
   ```

The greeting message comes from `catalog-service.yml` in `config-repo`, not from the local `application.yml`.

```console
→ java -jar build/libs/catalog-service-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod

→ http :9001/
Welcome to the production catalog from the config server
```

In this case, the greeting message is loaded from `catalog-service-prod.yml` in `config-repo`.

### 3.4 Recap (Section 4.4.1)

With Spring Cloud Config Client:

- Catalog Service uses the same immutable JAR.
- Configuration is centralized in `config-repo`.
- Config Service serves the right configuration based on:
   - `spring.application.name` (`catalog-service`)
   - `spring.profiles.active` (e.g. `prod`)

## 4. Making the Config Client resilient (Section 4.4.2)

These timeouts make the interaction with Config Service more resilient:
- `request-connect-timeout` – maximum time to wait for a TCP connection to the config server.
- `request-read-timeout` – maximum time to wait while reading configuration data from the server.

### 4.1 Timeouts for connecting to Config Service

These timeouts make the interaction with Config Service more resilient:
- `request-connect-timeout` – maximum time to wait for a TCP connection to the config server.
- `request-read-timeout` – maximum time to wait while reading configuration data from the server.

### 4.2 Retrying connection to Config Service (Spring Retry)

To make Catalog Service more resilient when Config Service is temporarily unavailable, the project uses Spring Retry:

- `spring-retry` dependency is added to `build.gradle.kts`.
- `spring.cloud.config.fail-fast=true` enables retry behavior for the config client.
- `spring.cloud.config.retry.*` controls:
  - `max-attempts` – maximum number of connection attempts.
  - `initial-interval` – initial delay before retrying (ms).
  - `max-interval` – maximum delay between retries (ms).
  - `multiplier` – factor used to compute the next delay (exponential backoff).

In local development, you may want to keep `fail-fast=false` to avoid hard failures when the config server is down.\
In production, you can enable `fail-fast` via externalized configuration.

## 5. Refreshing Configuration at Runtime (Section 4.4.3)

### 5.1 Enabling Actuator + Refresh Endpoint

Spring Boot Actuator exposes a `/actuator/refresh` endpoint that triggers a configuration refresh event.\
We explicitly expose this endpoint via `management.endpoints.web.exposure.include=refresh`.

### 5.2 Behavior of `@ConfigurationProperties` + `RefreshScopeRefreshedEvent`

The PolarProperties bean (defined with @ConfigurationProperties) automatically listens for RefreshScopeRefreshedEvent.\
When a refresh is triggered, it is reloaded with the latest configuration from Config Service, so you don't need to change the code.

### 5.3 Step-By-Step flow: Change Config at Runtime

1. Make sure both Config Service and Catalog Service are running:
   ```console
   → ./gradlew bootRun   # in config-service
   → ./gradlew bootRun   # in catalog-service
   ```
2. Open the `config-repo` and change the `polar.greeting` value in `catalog-service.yml`:
   ```yaml
   polar:
     greeting: "Welcome to the catalog from a fresh config server"
   ```
3. Commit and push the changes to the remote config repo.
4. Verify that Config Service returns the new value:
   ```console
   → http :8888/catalog-service/default
   ```
5. Trigger a refresh in Catalog Service:
   ```console
   → http POST :9001/actuator/refresh
   ```
6. Call the root endpoint again:
   ```console
   → http :9001/
   Welcome to the catalog from a fresh config server
   ```
7. Stop both applications with <kbd>CTRL</kbd>+<kbd>C</kbd> when you're done.

### 5.4 Recap (Section 4.4.3)

You've updated the configuration of a running application without restarting it or rebuilding the JAR, while keeping changes traceable in Git.\
This aligns with the 15-Factor methodology and cloud-native practices.

---

> For a high-level overview of Chapter 4 and links to other sections, see:  
> [`../README.md`](../README.md)
