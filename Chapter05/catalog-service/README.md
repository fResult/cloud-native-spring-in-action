# Catalog Service

This application is part of the Polar Bookshop system and provides the functionality for managing  the books in the bookshop catalog.\
It's part of the project built in the [Cloud Native Spring in Action](https://www.manning.com/books/cloud-native-spring-in-action) book by [Thomas Vitale](https://www.thomasvitale.com).

## REST API

|    Endpoint	    |  Method  | Req. body | Status | Resp. body | Description    		   	                     |
|:---------------:|:--------:|:---------:|:------:|:----------:|:------------------------------------------|
|    `/books`     |  `GET`   |           |  200   |   Book[]   | Get all the books in the catalog.         |
|    `/books`     |  `POST`  |   Book    |  201   |    Book    | Add a new book to the catalog.            |
|                 |          |           |  422   |            | A book with the same ISBN already exists. |
| `/books/{isbn}` |  `GET`   |           |  200   |    Book    | Get the book with the given ISBN.         |
|                 |          |           |  404   |            | No book with the given ISBN exists.       |
| `/books/{isbn}` |  `PUT`   |   Book    |  200   |    Book    | Update the book with the given ISBN.      |
|                 |          |           |  201   |    Book    | Create a book with the given ISBN.        |
| `/books/{isbn}` | `DELETE` |           |  204   |            | Delete the book with the given ISBN.      |

### Request Body: `version`

POST and PUT bind directly to the `Book` persistence record, without separate DTOs or DTO/entity mapping.\
Its `@Version int version` field must be present and non-null with the current JSON configuration; otherwise, request deserialization returns HTTP 400 (`Cannot map null into type int`).

For the local HTTPie examples, include **`version:=0`** to send a JSON number.\
Zero marks a new entity for POST and the PUT creation path.\
When updating an existing book, the service ignores the submitted version and saves using the version read from the database.\
It therefore does not detect stale client data by comparing request versions, even though JDBC optimistic locking still applies to the database update.

See [Why POST and PUT Include `version`](../README.md#why-post-and-put-include-version) for the Jackson background and complete examples.

## Useful Commands

| Gradle Command	            | Description                                   |
|:---------------------------|:----------------------------------------------|
| `./gradlew bootRun`        | Run the application.                          |
| `./gradlew build`          | Build the application.                        |
| `./gradlew test`           | Run tests.                                    |
| `./gradlew bootJar`        | Package the application as a JAR.             |
| `./gradlew bootBuildImage` | Package the application as a container image. |

After building the application, you can also run it from the Java CLI:

```bash
java -jar build/libs/catalog-service-0.0.1-SNAPSHOT.jar
```

## Running a PostgreSQL Database

Run PostgreSQL as a Docker container

```bash
docker run -d \
  --name polar-postgres \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=polardb_catalog \
  -p 5432:5432 \
  postgres:18-alpine
```

### Container Commands

| Docker Command	                |    Description    |
|:-------------------------------|:-----------------:|
| `docker stop polar-postgres`   |  Stop container.  |
| `docker start polar-postgres`  | Start container.  |
| `docker rm -fv polar-postgres` | Remove container. |

### Database Commands

Start an interactive PSQL console:

```bash
docker exec -it polar-postgres psql -U user -d polardb_catalog
```

| PSQL Command	              | Description                                    |
|:---------------------------|:-----------------------------------------------|
| `\list`                    | List all databases.                            |
| `\connect polardb_catalog` | Connect to specific database.                  |
| `\dt`                      | List all tables.                               |
| `\d book`                  | Show the `book` table schema.                  |
| `\d flyway_schema_history` | Show the `flyway_schema_history` table schema. |
| `\quit`                    | Quit interactive psql console.                 |

From within the PSQL console, you can also fetch all the data stored in the `book` table.

```bash
select * from book;
```

The following query is to fetch all the data stored in the `flyway_schema_history` table.

```bash
select * from flyway_schema_history;
```
