FROM mrray/blockchain-javaenv:0.0.1-SNAPSHOT
MAINTAINER docker_user docker_user@email.com

COPY chaincode/ /opt/blockchain-chaincode/src/main/java

WORKDIR /opt/blockchain-chaincode

RUN mvn clean package \
  && mkdir /opt/app \
  && cp -f /opt/blockchain-chaincode/target/blockchain-chaincode-0.0.1-SNAPSHOT-jar-with-dependencies.jar /opt/app

WORKDIR /opt/app

RUN rm -rf /opt/blockchain-chaincode \
  && rm -rf /opt/maven \
  && rm -f /usr/bin/mvn

EXPOSE 50054
CMD ["java", "-jar", "blockchain-chaincode-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]