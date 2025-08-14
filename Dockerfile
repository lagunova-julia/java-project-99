FROM eclipse-temurin:17-jdk

RUN apt-get update && apt-get install -yq make unzip

COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .

ARG SENTRY_AUTH_TOKEN

# ENV SENTRY_AUTH_TOKEN=$SENTRY_AUTH_TOKEN

RUN ./gradlew --no-daemon dependencies

COPY config config
COPY src src

RUN ./gradlew --no-daemon build -PsentryAuthToken=$SENTRY_AUTH_TOKEN

EXPOSE 8080

CMD java -jar build/libs/demo-0.0.1-SNAPSHOT.jar
