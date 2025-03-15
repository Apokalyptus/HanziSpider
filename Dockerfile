FROM docker.io/openjdk:23
ADD http://192.168.178.240:8061/repository/raw/HanziSpider/HanziSpider-1.0-SNAPSHOT-jar-with-dependencies.jar HanziSpider-1.0-SNAPSHOT-jar-with-dependencies.jar
COPY entrypoint.sh /
ENV JAVA_OPTS=""
ENTRYPOINT ["/entrypoint.sh"] 
