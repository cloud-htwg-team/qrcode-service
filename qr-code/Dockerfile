# syntax = docker/dockerfile:experimental

FROM gradle:7.5.1-jdk17 AS builder
COPY --chown=gradle:gradle . /build
WORKDIR /build
RUN gradle build --no-daemon
RUN echo $(ls -1 /build/build)


### RUN Image ##
FROM eclipse-temurin:17
EXPOSE 8080
WORKDIR /app

COPY --from=builder /build/build/libs/*.jar /app/qr-code-microservice.jar

ENTRYPOINT ["java", "-jar","/app/qr-code-microservice.jar"]





### BUILD image ##
#FROM gradle:7.5.1-jdk17 AS builder
#WORKDIR /build
#
## COPY config files
#COPY ./docker/global.settings.gradle ./settings.gradle
#COPY ./docker/global.build.gradle ./build.gradle
#
## Copy files
#COPY docker ./qr-code-api
#
## Run build
#RUN \
#    --mount=type=cache,id=gradle,target=/root/.gradle \
#    --mount=type=cache,id=gradle,target=/home/gradle/.gradle \
#    --mount=type=cache,id=gradle-qr-code-api,target=/build/qr-code-api/.gradle \
#    --mount=type=cache,id=gradle-qr-code-api-build,target=/build/qr-code-api/build \
#    gradle --no-daemon build
#
## extract layers
#RUN --mount=type=cache,id=gradle-qr-code-api-build,target=/build/qr-code-api/build \
#    java -Djarmode=layertools -jar ./qr-code-api/build/libs/qr-code-api.jar extract
#
### RUN Image ##
#FROM eclipse-temurin:11
#WORKDIR /app
#
#COPY --from=builder /build/dependencies /app
#COPY --from=builder /build/snapshot-dependencies /app
#COPY --from=builder /build/spring-boot-loader /app
#COPY --from=builder /build/application /app
#ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]


