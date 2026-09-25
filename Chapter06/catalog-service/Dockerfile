# Builder Container
FROM eclipse-temurin:26-jre AS builder
WORKDIR /builder
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} catalog-service.jar
RUN java -Djarmode=tools -jar catalog-service.jar extract --layers --destination extracted

# Runtime Container
FROM eclipse-temurin:26-jre
WORKDIR /workspace
RUN useradd spring
USER spring
COPY --from=builder /builder/extracted/dependencies ./
COPY --from=builder /builder/extracted/spring-boot-loader ./
COPY --from=builder /builder/extracted/snapshot-dependencies ./
COPY --from=builder /builder/extracted/application ./

ENTRYPOINT ["java", "-jar", "catalog-service.jar"]
