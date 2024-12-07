# Dockerfile

# jdk17 Image Start
FROM openjdk:17

ARG JAR_FILE=build/libs/food-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} food.jar
ENTRYPOINT ["java","-jar","-Duser.timezone=Asia/Seoul","food.jar"]
