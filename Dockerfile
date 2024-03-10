FROM adoptopenjdk/openjdk11
EXPOSE 8080
ARG JAR_FILE=build/libs/*-SNAPSHOT.jar
ARG PINPOINT_AGENT_DIR=pinpoint-agent-2.5.3
COPY ${JAR_FILE} app.jar
COPY ${PINPOINT_AGENT_DIR} /pinpoint-agent

ENTRYPOINT ["java", "-jar", "-javaagent:/pinpoint-agent/pinpoint-bootstrap-2.5.3.jar", "-Dpinpoint.agentId=modoospace-release", "-Dpinpoint.applicationName=modoospace-release", "-Dpinpoint.profiler.profiles.active=release", "app.jar"]
