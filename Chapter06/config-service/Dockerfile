# Builder Container
FROM eclipse-temurin:26-jre AS builder
WORKDIR /builder
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} config-service.jar
RUN java -Djarmode=tools -jar config-service.jar extract --layers --destination extracted

# Runtime Container
FROM eclipse-temurin:26-jre
WORKDIR /workspace
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
RUN useradd --create-home spring
USER spring
COPY --from=builder /builder/extracted/dependencies ./
COPY --from=builder /builder/extracted/spring-boot-loader ./
COPY --from=builder /builder/extracted/snapshot-dependencies ./
COPY --from=builder /builder/extracted/application ./

ENTRYPOINT ["java", "-jar", "config-service.jar"]
