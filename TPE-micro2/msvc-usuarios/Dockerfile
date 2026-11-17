FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system ticketya && useradd --system --gid ticketya ticketya
COPY --from=build /build/target/msvc-usuarios-0.0.1-SNAPSHOT.jar app.jar
USER ticketya
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
