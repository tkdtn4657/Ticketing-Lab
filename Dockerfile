FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace

COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x ./gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

USER app

EXPOSE 9090

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
