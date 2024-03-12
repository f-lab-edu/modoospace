FROM adoptopenjdk/openjdk11
EXPOSE 8080
ARG JAR_FILE=build/libs/*-SNAPSHOT.jar
ARG PINPOINT_AGENT_DIR=pinpoint-agent-2.2.3-NCP-RC1
COPY ${JAR_FILE} app.jar
COPY ${PINPOINT_AGENT_DIR} /pinpoint-agent
ENV TZ=Asia/Seoul

ENTRYPOINT ["java", "-jar", "-javaagent:/pinpoint-agent/pinpoint-bootstrap-2.2.3-NCP-RC1.jar", "-Dpinpoint.agentId=modoospace-release", "-Dpinpoint.applicationName=modoospace-release", "app.jar"]
