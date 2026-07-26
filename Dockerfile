FROM eclipse-temurin:21-jre-alpine
LABEL authors="subhammukherjee"

WORKDIR /app

COPY target/*.jar app.jar

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 5059

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
