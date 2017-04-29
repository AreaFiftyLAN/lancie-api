FROM wisvch/alpine-java:8_server-jre_unlimited 
ADD build/libs/lancie-api.jar /srv/lancie-api.jar
WORKDIR /srv
CMD "/srv/lancie-api.jar"
