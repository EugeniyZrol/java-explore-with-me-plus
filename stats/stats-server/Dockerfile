FROM eclipse-temurin:11-jre-alpine
VOLUME /tmp
COPY target/stats-server-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]