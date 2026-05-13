FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace

COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x ./gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache tzdata su-exec \
    && addgroup -S app \
    && adduser -S app -G app \
    && mkdir -p /app/logs \
    && chown -R app:app /app/logs

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

EXPOSE 9090

ENV TZ=Asia/Seoul
ENV JAVA_OPTS="-Duser.timezone=Asia/Seoul"

ENTRYPOINT ["sh", "-c", "mkdir -p /app/logs && chown -R app:app /app/logs && exec su-exec app:app java $JAVA_OPTS -jar /app/app.jar"]
