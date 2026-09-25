# Stage 1: Build Quarkus fast-jar application
FROM docker.io/library/maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy maven wrapper and descriptor
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B || true

# Copy source code and build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# Stage 2: Production runtime image
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.24

ENV LANGUAGE='en_US:en'

# Copy the Quarkus fast-jar application artifacts
COPY --chown=185:185 --from=build /build/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185:185 --from=build /build/target/quarkus-app/*.jar /deployments/
COPY --chown=185:185 --from=build /build/target/quarkus-app/app/ /deployments/app/
COPY --chown=185:185 --from=build /build/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
