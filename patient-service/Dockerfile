FROM gradle:8.13-jdk21 AS builder

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon && \
    gradle --no-daemon help && \
    gradle --no-daemon tasks

COPY src ./src

RUN gradle clean build -x test --no-daemon

FROM openjdk:21-jdk AS runner

WORKDIR /app

COPY --from=builder ./app/build/libs/patient-service-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 4000

ENTRYPOINT ["java", "-jar", "app.jar"]