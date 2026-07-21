FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache wget \
    && addgroup -S fiapx && adduser -S fiapx -G fiapx
ARG JAR_FILE=build/libs/*.jar
COPY --chown=fiapx:fiapx ${JAR_FILE} app.jar
USER fiapx
EXPOSE 8084
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

