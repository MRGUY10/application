# Étape 1 : Builder avec Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copier le projet Maven
COPY . .

# Compiler le projet et générer le JAR
RUN mvn clean package -DskipTests

# Étape 2 : Exécuter le JAR
FROM openjdk:17

WORKDIR /app

# Copier le JAR depuis l'étape précédente
COPY --from=build /app/target/*.jar application2.jar

ENTRYPOINT ["java", "-jar", "application2.jar"]