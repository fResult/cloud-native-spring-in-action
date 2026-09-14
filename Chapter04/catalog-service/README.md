# Catalog Service - Externalized Configuration

This guide demonstrates how to **externalize application configuration** and override configuration properties at runtime using the **same immutable executable JAR artifact** (no rebuild required).

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

## 2. Running with Default Configuration (`application.yml`)

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

## 3. Configuring via Command-Line Arguments (4.2.1)

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

## 4. Configuring via JVM System Properties (4.2.2)

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

## 5. Property Precedence: CLI vs JVM vs `application.yml`

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

## 6. Configuring via Environment Variables (4.2.3)

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

### 6.1 Overriding `polar.greeting` via Environment Variable (Linux/macOS)

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

### 6.2 Overriding via Environment Variable (Windows PowerShell)

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

## 7. Why Environment Variables Are Preferred

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

## 8. Recap

Using a single immutable JAR (`catalog-service-0.0.1-SNAPSHOT.jar`), you can externalize configuration by:

1. **Command-line arguments**\
   `--polar.greeting="Welcome to the catalog from CLI"`
2. **JVM system properties**\
   `-Dpolar.greeting="Welcome to the catalog from JVM"`
3. **Environment variables** (recommended)\
   `POLAR_GREETING="Welcome to the catalog from ENV"`

All of these let you change configuration **without rebuilding** the application, aligning with cloud-native and 15-Factor principles.

> For a high-level overview of Chapter 4 and links to other sections, see:  
> [`../README.md`](../README.md)
