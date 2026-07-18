FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache wget
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app.jar"]

