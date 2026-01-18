FROM docker.io/cimg/openjdk:21.0.9-browsers
USER root
WORKDIR /app
COPY target/HanziSpider-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh
ENV JAVA_OPTS=""
ENTRYPOINT ["./entrypoint.sh"]