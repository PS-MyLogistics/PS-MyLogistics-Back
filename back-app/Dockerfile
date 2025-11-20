# ---- STAGE 1: Build ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY . .
RUN chmod +x mvnw
RUN ./mvnw -DskipTests=true package

# ---- STAGE 2: Run ----
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
