# 1. Explicit base image version (Java 21 JRE slim for smaller attack surface)
FROM eclipse-temurin:21-jre-jammy

# 2. Define explicit environment parameters and working directory
ENV SERVER_PORT=8080
WORKDIR /app

# 3. Copy the packaged JAR explicitly (avoiding ADD)
COPY target/spring-petclinic-*.jar app.jar

# 4. Least-Privilege Execution: Create and switch to a non-root user
RUN useradd -m -u 1001 petclinicuser && chown -R petclinicuser:petclinicuser /app
USER petclinicuser

EXPOSE 8080

# 5. Exec-form entrypoint definition
CMD ["java", "-jar", "app.jar"]