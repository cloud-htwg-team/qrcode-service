# syntax = docker/dockerfile:experimental

FROM gradle:7.5.1-jdk17 AS builder
COPY --chown=gradle:gradle . /build
WORKDIR /build
RUN gradle build --no-daemon
#RUN echo $(ls -1 /build/build/libs)
#RUN ls -1 /build/build/libs

### RUN Image ##
FROM eclipse-temurin:17
EXPOSE 8080
WORKDIR /app

COPY --from=builder /build/build/libs/qr-code-microservice.jar /app/qr-code-microservice.jar

ENTRYPOINT ["java", "-jar","/app/qr-code-microservice.jar"]

# override server.port of the app (inside of the container)
#ENTRYPOINT ["java", "-jar","/app/qr-code-microservice.jar", "--server.port=8089"]

