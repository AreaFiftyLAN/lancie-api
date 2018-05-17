FROM wisvch/spring-boot-base:1
COPY ./build/libs/lancie-api.jar /srv/lancie-api.jar
CMD ["/srv/lancie-api.jar"]
