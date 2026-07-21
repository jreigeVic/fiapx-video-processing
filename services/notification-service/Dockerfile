FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache wget \
    && addgroup -S fiapx && adduser -S fiapx -G fiapx
ARG JAR_FILE=build/libs/*.jar
COPY --chown=fiapx:fiapx ${JAR_FILE} app.jar
# OpenTelemetry Java agent (ADR-008, implemented via ADR-015): always
# present in the image, but inert here - the 3 exporters default to
# "none" so docker-compose/local/CI behavior is unchanged. Helm
# (infrastructure/helm) turns exporting on for the real deployment.
RUN wget -O /opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.10.0/opentelemetry-javaagent.jar
USER fiapx
EXPOSE 8084
ENV OTEL_TRACES_EXPORTER=none \
    OTEL_METRICS_EXPORTER=none \
    OTEL_LOGS_EXPORTER=none
ENTRYPOINT ["java","-javaagent:/opentelemetry-javaagent.jar","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

