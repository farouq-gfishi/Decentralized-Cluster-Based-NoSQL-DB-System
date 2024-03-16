FROM openjdk:17-alpine
WORKDIR /app
ADD target/*.jar app.jar
RUN mkdir -p /app/DataBase
ENTRYPOINT ["java", "-jar", "app.jar"]