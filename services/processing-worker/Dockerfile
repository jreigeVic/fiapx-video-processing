FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache ffmpeg wget
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app.jar"]

