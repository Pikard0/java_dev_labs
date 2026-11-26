FROM eclipse-temurin:17-jre

ARG JAR_FILE=target/catalog-service-1.0.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app.jar"]
