FROM wisvch/spring-boot-base:1
COPY ./build/libs/lancie-api.jar /srv/lancie-api.jar
USER spring-boot
CMD ["/srv/lancie-api.jar"]
