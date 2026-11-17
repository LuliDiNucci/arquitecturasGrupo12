FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system ticketya \
    && useradd --system --gid ticketya ticketya \
    && install -d -o ticketya -g ticketya /app/data
COPY --from=build /build/target/msvc-notificaciones-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8087
EXPOSE 8087
USER ticketya
ENTRYPOINT ["java", "-jar", "app.jar"]
