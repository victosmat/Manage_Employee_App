# Sử dụng một hình ảnh maven để xây dựng ứng dụng Spring Boot
FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Sử dụng hình ảnh OpenJDK để triển khai ứng dụng Spring Boot
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/manage-employee-app.jar .

# Chạy ứng dụng Spring Boot
CMD ["java", "-jar", "manage-employee-app.jar"]
