FROM openjdk:17
ADD /target/messen-0.0.1-SNAPSHOT.jar chat.jar
ENTRYPOINT "java", "-jar","chat.jar"