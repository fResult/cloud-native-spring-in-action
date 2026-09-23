# Chapter 5 – Persisting and Managing Data in the Cloud

This chapter focuses on persisting and managing application data in cloud native Spring applications.

You start by:

- Exploring **databases for cloud native systems**, data services in the cloud, and running **PostgreSQL as a container** (5.1)
- Implementing persistence with **Spring Data JDBC**, including database connections, persistent entities, auditing, and repositories (5.2)

Then you:

- Test data persistence with **Testcontainers**, using both **@DataJdbcTest** and **@SpringBootTest** (5.3)
- Manage database schema changes with **Flyway**, from schema initialization to versioned migrations (5.4)

This builds on the cloud native principles introduced in Chapters 1–2, the RESTful Catalog Service developed in Chapter 3, and the externalized configuration from Chapter 4. Chapter 6 continues with packaging Spring Boot applications as container images, managing them with Docker Compose, and publishing them through a deployment pipeline.

> [!NOTE]
> This repository includes local PostgreSQL setup, JDBC persistence and auditing, repository and application tests, and two Flyway migrations. The examples below focus on that local development flow; managed cloud database provisioning and production database operations are not documented here.

For reference, the official source code for this chapter is available at:

> [ThomasVitale/cloud-native-spring-in-action/Chapter05/05-end](https://github.com/ThomasVitale/cloud-native-spring-in-action/tree/main/Chapter05/05-end)

## 1. Prerequisites

- **JDK 26**, matching the Catalog Service Gradle toolchain
- **Docker** running locally, for PostgreSQL and Testcontainers
- **HTTPie**, for the API examples
- The project's **Gradle Wrapper** (`./gradlew`)

## 2. Project Structure

| Directory | Role in this chapter |
| --- | --- |
| [catalog-service/](catalog-service/) | Catalog API with PostgreSQL persistence, JDBC auditing, Testcontainers tests, and Flyway migrations |
| [config-service/](config-service/) | Spring Cloud Config Server carried forward from Chapter 4 |
| [config-repo/](config-repo/) | Configuration files carried forward from Chapter 4 |

The configuration modules support the existing application setup. The new persistence work is in `catalog-service/`.

## 3. Chapter Overview (Mapping to the Table of Contents)

### 5.1 Databases for Cloud Native Systems

The book introduces data services in the cloud and running PostgreSQL as a container. The hands-on example here uses a local PostgreSQL container as the Catalog Service's backing service.

See [Prepare the Database](#4-prepare-the-database) for the container command and connection settings.

### 5.2 Data Persistence with Spring Data JDBC

The Catalog Service connects to PostgreSQL through JDBC and a HikariCP connection pool.

| Topic | Implementation |
| --- | --- |
| Connecting to a database with JDBC | [application.yml](catalog-service/src/main/resources/application.yml) |
| Defining persistent entities | [Book.java](catalog-service/src/main/java/com/polarbookshop/catalogservice/domain/Book.java), including identity and version fields |
| Enabling and configuring JDBC auditing | [DataConfiguration.java](catalog-service/src/main/java/com/polarbookshop/catalogservice/common/config/DataConfiguration.java) and the creation/modification timestamps on `Book` |
| Data repositories | [BookRepository.java](catalog-service/src/main/java/com/polarbookshop/catalogservice/domain/BookRepository.java) |

### 5.3 Testing Data Persistence with Spring and Testcontainers

[TestContainersConfiguration.java](catalog-service/src/test/java/com/polarbookshop/catalogservice/config/TestContainersConfiguration.java) declares a shared PostgreSQL container configuration using `@TestConfiguration`, `@Bean`, and `@ServiceConnection`.

Both test classes import this configuration:

- [BookRepositoryJdbcTest.java](catalog-service/src/test/java/com/polarbookshop/catalogservice/domain/BookRepositoryJdbcTest.java) tests repository operations with `@DataJdbcTest`.
- [CatalogServiceApplicationTests.java](catalog-service/src/test/java/com/polarbookshop/catalogservice/CatalogServiceApplicationTests.java) tests application behavior with `@SpringBootTest`.

The declaration is shared; each distinct Spring application context gets its own container bean. These tests use Testcontainers-managed databases and do not require the manually started `polar-postgres` container.

See [Run Persistence Tests](#6-run-persistence-tests) for the commands.

### 5.4 Managing Databases in Production with Flyway

Flyway provides version control for the database schema. This project demonstrates initialization and evolution through SQL migrations in [db/migration/](catalog-service/src/main/resources/db/migration/):

| Migration                                                                                                    | Change                                                            |
|--------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------|
| [V1__initial_schema.sql](catalog-service/src/main/resources/db/migration/V1__initial_schema.sql)             | Creates the `books` table, including auditing and version columns |
| [V2__Add_publisher_column.sql](catalog-service/src/main/resources/db/migration/V2__Add_publisher_column.sql) | Adds the `publisher` column                                       |

## 4. Prepare the Database

Run PostgreSQL as a container on your local machine:

```bash
docker run -d \
  --name polar-postgres \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=polardb_catalog \
  -p 5432:5432 \
  postgres:18-alpine
```

These settings match the local datasource configuration in `catalog-service/src/main/resources/application.yml`.

Stop and restart the existing container with:

```bash
docker stop polar-postgres
docker start polar-postgres
```

To reset the local database, remove the container and its anonymous volumes, then repeat the `docker run` command. This deletes the database data:

```bash
docker rm -fv polar-postgres
```

## 5. Run the Application and Test the API

From `Chapter05/catalog-service/`, start the application:

```bash
./gradlew bootRun
```

The local configuration points to PostgreSQL on port `5432` and exposes the API on port `9001`.\
The Config Server import is optional for this local flow.

### Why POST and PUT Include `version`

The controller binds request JSON directly to the [Book record](catalog-service/src/main/java/com/polarbookshop/catalogservice/domain/Book.java), which also serves as the persistence entity and response body.\
This learning project does not introduce separate DTOs or DTO/entity mapping, so persistence fields are exposed in the API model.

`Book` declares `@Version int version`.\
With the application's current Jackson configuration, omitting `version` or sending `null` causes deserialization to fail with `Cannot map null into type int`, returning HTTP 400 before the service or repository is called.\
This is a JSON binding error, not an optimistic-locking conflict.\
Jackson 3.0 enabled `FAIL_ON_NULL_FOR_PRIMITIVES` by default; see the [Jackson migration guide](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md).\
The behavior described here is for this project's current setup, rather than every Jackson version or configuration.

Use HTTPie's `:=` syntax to send a JSON number: `version:=0` produces `"version": 0`, whereas `version=0` sends the string `"0"` and relies on coercion.

| Request                         | Value used in these examples | Meaning in the current implementation                                                                                                                       |
|---------------------------------|------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| POST a new book                 | `version:=0`                 | Zero marks a new entity for Spring Data JDBC's primitive version field.                                                                                     |
| PUT an existing book            | `version:=0`                 | Satisfies JSON binding. `BookService.editBookDetails()` uses `existingBook.version()` loaded from the database when saving, ignoring the submitted version. |
| PUT an ISBN that does not exist | `version:=0`                 | The service follows the creation path, so zero represents a new entity.                                                                                     |

> [!NOTE]
> Spring Data JDBC still uses the entity version for optimistic locking during the database update.\
> However, the current service does not compare the client's version with the stored version, so a successful PUT does not prove that the client edited the latest representation.\
> Supplying a version in the request is a compatibility measure for this shared API/persistence model, not a complete HTTP concurrency-control design.

### HTTPie Examples

In another terminal, create a book with an ISBN that is not already in the database:

```bash
http POST :9001/books \
  isbn="1234567891" title="Northern Lights" price:=12.90 \
  author="Lyra Silverstar" publisher="Polarsophia" version:=0
```

Retrieve it, then update its title and price:

```bash
http GET :9001/books/1234567891

http PUT :9001/books/1234567891 \
  isbn="1234567891" title="Northern Countries" price:=19.90 \
  author="Lyra Silverstar" publisher="Polarsophia" version:=0
```

Inspect the returned `id`, `createdDate`, `lastModifiedDate`, and `version` fields alongside the book details.\
IDs and timestamps depend on your database state and execution time.

## 6. Run Persistence Tests

With Docker running, execute these commands from `Chapter05/catalog-service/`.

Run the repository tests:

```bash
./gradlew test --tests '*BookRepositoryJdbcTest'
```

Run the application integration tests:

```bash
./gradlew test --tests '*CatalogServiceApplicationTests'
```

Run the full test suite:

```bash
./gradlew test
```
