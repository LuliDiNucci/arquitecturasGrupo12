FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
# Contexto: TPE-micro2. La biblioteca no impone dependencias transitivas ni versiones.
COPY observability ./observability
RUN mvn -B -f observability/pom.xml -DskipTests install
COPY msvc-reservas/pom.xml pom.xml
RUN mvn -B dependency:go-offline
COPY msvc-reservas/src ./src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system --gid 10001 ticketya \
    && useradd --system --uid 10001 --gid ticketya ticketya \
    && install -d -o ticketya -g ticketya /app/data /var/log/ticketya
COPY --from=build /build/target/msvc-reservas-0.0.1-SNAPSHOT.jar app.jar
ENV TICKETYA_LOG_DIR=/var/log/ticketya
USER ticketya
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
