# 빌드 단계
FROM gradle:8.4-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build -x test --no-daemon

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 9000
