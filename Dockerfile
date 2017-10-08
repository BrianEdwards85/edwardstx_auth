FROM openjdk:8
RUN mkdir /etc/serivce 
COPY ./target/auth.jar /srv/auth.jar
WORKDIR /srv

EXPOSE 5002

ENTRYPOINT /usr/bin/java -Dconfig="/etc/service/edwardstx_auth.edn" -jar /srv/auth.jar


