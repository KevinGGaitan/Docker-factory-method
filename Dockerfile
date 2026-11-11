FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY Docker-factory-method ./Docker-factory-method
COPY implementacion ./implementacion
COPY utilidades ./utilidades
RUN mvn -q -DskipTests package dependency:copy-dependencies -DoutputDirectory=target/dependency

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/classes ./classes
COPY --from=build /app/target/dependency ./dependency
CMD ["java", "-cp", "classes:dependency/*", "patronfactorymethod.PatronFactoryMethod"]
