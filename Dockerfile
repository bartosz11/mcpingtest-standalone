FROM eclipse-temurin:17-jre-jammy
RUN useradd -d /home/container -s /bin/bash -m container
COPY build/libs/mcpingtest-server.jar /bin/server.jar
WORKDIR /home/container
VOLUME /home/container
USER container
ENTRYPOINT exec java -jar /bin/server.jar