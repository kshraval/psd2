
FROM java
MAINTAINER tanmay ambre
COPY ./target/oauth2server-0.0.1-SNAPSHOT.jar /usr/local/oauth2server/
RUN ls -al /usr/local/oauth2server
RUN java -version
EXPOSE 8081
VOLUME /logs
WORKDIR /usr/local/oauth2server
ENTRYPOINT java -jar oauth2server-0.0.1-SNAPSHOT.jar
