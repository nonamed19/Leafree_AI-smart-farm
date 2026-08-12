# 빌드 단계
FROM gradle:8.4-jdk17 AS builder
WORKDIR /app/backend
COPY backend /app/backend
COPY AI/plant_creators/Document_Parser/web_crawling/src/main/java /app/AI/plant_creators/Document_Parser/web_crawling/src/main/java
RUN gradle build -x test --no-daemon

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/backend/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 9000
