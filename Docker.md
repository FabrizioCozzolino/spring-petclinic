Containerization & Docker Report

1. Overview

This report documents the containerization setup for Spring PetClinic (FabrizioCozzolino/spring-petclinic), covering single-container hygiene, multi-container orchestration with Docker Compose, and automated DockerHub publishing via GitHub Actions.

2. Dockerfile & Hygiene Best Practices

FROM eclipse-temurin:21-jre-jammy
ENV SERVER_PORT=8080
WORKDIR /app
COPY target/spring-petclinic-*.jar app.jar
RUN useradd -m -u 1001 petclinicuser && chown -R petclinicuser:petclinicuser /app
USER petclinicuser
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]


Anti-Patterns Avoided

No :latest Base Image: Explicitly pinned to eclipse-temurin:21-jre-jammy (JRE-only slim image) to guarantee deterministic builds and reduce attack surface.

No Hardcoded Secrets: Database credentials are injected externally via environment variables and .env (excluded from Git), never baked into image layers.

Least-Privilege Execution (USER): Instead of running as root, the container switches to an unprivileged user (petclinicuser, UID 1001), mitigating container breakout risks.

COPY instead of ADD: Prevents implicit archive extraction or remote URL fetching side effects.

Exec-Form CMD: Uses JSON array syntax (["java", "-jar", "app.jar"]) so the JVM runs as PID 1 and properly receives Unix signals (SIGTERM) for graceful shutdown.

3. Multi-Container Orchestration (docker-compose.yml)

services:
  db:
    image: mysql:8.0.36
    container_name: petclinic-db
    restart: on-failure
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - db_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 20s

  web:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: petclinic-web
    restart: on-failure
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: mysql
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
    depends_on:
      db:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
      interval: 15s
      timeout: 5s
      retries: 5
      start_period: 30s

volumes:
  db_data:


Dependability Mechanisms

Data Durability: The named volume db_data decouples MySQL storage from container lifecycle, ensuring data survives docker compose down.

Health Checks & Startup Ordering: depends_on with condition: service_healthy prevents web from failing at startup while MySQL initializes.

Fault Recovery: restart: on-failure automatically restarts crashed containers.

4. Reference Workflow Audit (docker-build.yml)

Trigger Mechanism: Uses workflow_run listening to types: [completed] on the CI workflow (main branch), combined with an if: github.event.workflow_run.conclusion == 'success' guard so broken code is never packaged into an image.

Secrets Management: Authenticates to DockerHub via docker/login-action reading encrypted GitHub Actions secrets (${{ secrets.DOCKER_USERNAME }} and ${{ secrets.DOCKER_PASSWORD }} Personal Access Token).

Multi-Platform Support: Uses Docker Buildx with platforms: linux/amd64,linux/arm64 to generate multi-architecture manifests compatible with both x86_64 cloud servers and ARM64 devices.

Caching: Optimizes build latency at two levels: Maven dependency caching (~/.m2) and GitHub Actions Buildx layer caching (cache-from: type=gha, cache-to: type=gha,mode=max).

Metadata Handling: Uses docker/metadata-action to automatically generate OCI labels and image tags (latest and commit SHA tags) dynamically at build time.