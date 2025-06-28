FROM eclipse-temurin:17-jre

WORKDIR /app

COPY app/build/libs/app-all.jar /app/app-all.jar

EXPOSE 10000

CMD ["java", "-jar", "app-all.jar"]
