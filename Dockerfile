# Use the official OpenJDK 11 image as the base image
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17
#FROM openjdk:17
 
# Set the working directory in the container
WORKDIR /app
 
# Copy the packaged JAR file into the container
COPY target/cove-user-service-0.0.1-SNAPSHOT.jar /app/cove-user-service-0.0.1-SNAPSHOT.jar
 
# Expose the port that the application runs on
EXPOSE 8090
 
# Run the JAR file when the container launches
CMD ["java", "-jar", "cove-user-service-0.0.1-SNAPSHOT.jar"]