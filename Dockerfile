FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY target/projeto-base-spring-boot.jar projeto-base-spring-boot.jar

EXPOSE 80

ENTRYPOINT ["java", "-jar", "projeto-base-spring-boot.jar"]
