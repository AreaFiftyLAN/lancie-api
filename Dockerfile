FROM wisvch/openjdk:11-jdk AS builder
COPY . /src
WORKDIR /src
RUN ./gradlew build

FROM wisvch/spring-boot-base:2.1
COPY --from=builder /src/build/libs/lancie-api.jar /srv/lancie-api.jar
CMD ["/srv/lancie-api.jar"]
