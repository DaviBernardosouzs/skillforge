# syntax=docker/dockerfile:1

# 1. build: dependências primeiro, para o cache de camadas sobreviver a mudanças no código
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/
RUN ./mvnw -B -q dependency:go-offline
COPY src/ src/
# Os testes rodam no CI, não no build da imagem
RUN ./mvnw -B -q package -DskipTests

# 2. layers: separa o jar em camadas (dependências mudam pouco, o código muda sempre)
FROM eclipse-temurin:21-jre-alpine AS layers
WORKDIR /layers
COPY --from=build /build/target/skillforge-*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# 3. runtime: JRE enxuta, usuário sem privilégios
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S skillforge && adduser -S -G skillforge skillforge \
    && mkdir -p /app/tmp && chown -R skillforge:skillforge /app
WORKDIR /app
COPY --from=layers --chown=skillforge:skillforge /layers/extracted/dependencies/ ./
COPY --from=layers --chown=skillforge:skillforge /layers/extracted/spring-boot-loader/ ./
COPY --from=layers --chown=skillforge:skillforge /layers/extracted/snapshot-dependencies/ ./
COPY --from=layers --chown=skillforge:skillforge /layers/extracted/application/ ./
USER skillforge

# Sobrescreva JAVA_TOOL_OPTIONS para ajustar a memória ao limite do container
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080

# wget vem no BusyBox da imagem Alpine
HEALTHCHECK --interval=15s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1

# java.io.tmpdir aponta para uma pasta do usuário skillforge: é onde os repositórios são gerados
ENTRYPOINT ["java", "-Djava.io.tmpdir=/app/tmp", "org.springframework.boot.loader.launch.JarLauncher"]
