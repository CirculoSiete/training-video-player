FROM adoptopenjdk:11.0.5_10-jre-openj9-0.17.0-bionic
COPY build/libs/training-video-player-*-all.jar training-video-player.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar training-video-player.jar