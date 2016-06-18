FROM java
MAINTAINER tanmay ambre
COPY ./target/psd2api-0.0.1-SNAPSHOT.jar /usr/local/psd2api/
RUN ls -al /usr/local/psd2api
RUN java -version
EXPOSE 8082
VOLUME /logs
WORKDIR /usr/local/psd2api
ENTRYPOINT java -jar psd2api-0.0.1-SNAPSHOT.jar
