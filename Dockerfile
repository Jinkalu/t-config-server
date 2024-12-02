# Stage 1: Build Stage
FROM maven:3.9.5 AS build

ARG CONTAINER_PORT

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and other necessary files to download dependencies
COPY pom.xml .

# Download dependencies (this helps with caching dependencies in Docker layers)
RUN mvn dependency:go-offline

# Copy the source code to the container
COPY src ./src

# Run Maven build to generate the JAR file
RUN mvn clean package -DskipTests

# Stage 2: Runtime Stage
FROM eclipse-temurin:21-jre

# Set the working directory for runtime
WORKDIR /application

# Copy the generated JAR file from the builder stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE ${CONTAINER_PORT}

# Run the application using the Spring Boot loader
ENTRYPOINT ["java", "-jar", "app.jar"]


#FROM maven:3.9.5 AS build
#WORKDIR /app
#ARG CONTAINER_PORT
#COPY pom.xml /app
#RUN mvn dependency:resolve
#COPY . /app
#RUN mvn clean
#RUN mvn package -DskipTest -X
#
#FROM openjdk:21
#COPY --from=build /app/target/*.jar app.jar
#EXPOSE ${CONTAINER_PORT}
#CMD ["java","-jar","app.jar"]

