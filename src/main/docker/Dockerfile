FROM openjdk:8-jdk
MAINTAINER docker_user docker_user@email.com
RUN mkdir /opt/app
COPY md_blockchain_peer-0.0.1-SNAPSHOT.jar /opt/app

WORKDIR /opt/app

ENV BLOCKCHAIN_ENVIRONMENT=DOCKER

EXPOSE 8080
EXPOSE 6789
EXPOSE 50051
CMD ["java", "-jar", "md_blockchain_peer-0.0.1-SNAPSHOT.jar"]