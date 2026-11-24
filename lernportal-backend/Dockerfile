# Build stage
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon && \
    mv /app/build/libs/*.jar /app/app.jar

# Runtime stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/app.jar ./
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]