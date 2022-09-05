FROM docker.io/openjdk:11-jre-slim
ADD http://192.168.31.162:8082/ui/native/generic-local/HanzeSpider-1.0-SNAPSHOT-jar-with-dependencies.jar HanzeSpider-1.0-SNAPSHOT-jar-with-dependencies.jar
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -jar /HanzeSpider-1.0-SNAPSHOT-jar-with-dependencies.jar
