FROM openjdk:8-jdk
COPY target/*.jar swarm-test.jar
EXPOSE 8000
ENTRYPOINT ["java","-jar","swarm-test.jar"]