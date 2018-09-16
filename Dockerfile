FROM wisvch/openjdk:10-jdk AS builder
COPY . /src
WORKDIR /src
RUN ./gradlew build

FROM wisvch/spring-boot-base:2
COPY --from=builder /src/build/libs/lancie-api.jar /srv/lancie-api.jar
CMD ["/srv/lancie-api.jar"]
