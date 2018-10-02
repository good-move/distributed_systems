FROM openjdk:8u181

ENV JAR_NAME ChatNode.jar
ENV PATH_TO_JAR ./target/scala-2.12/$JAR_NAME

EXPOSE $CLUSTER_PORT

COPY $PATH_TO_JAR /
COPY run.sh /

ENTRYPOINT ["/run.sh"]
CMD []
