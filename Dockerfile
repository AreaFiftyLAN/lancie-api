FROM ghcr.io/wisvch/spring-boot-base:2.1
COPY ./build/libs/lancie-api.jar /srv/lancie-api.jar
CMD ["/srv/lancie-api.jar"]