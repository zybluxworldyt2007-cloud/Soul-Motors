# syntax=docker/dockerfile:1

# ---------- Etapa 1: compilar con Maven ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Primero solo el pom para cachear las dependencias entre builds
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q -DskipTests package && cp target/*.jar app.jar

# ---------- Etapa 2: imagen liviana solo con el JRE ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# No correr como root
RUN useradd --system --uid 1001 --no-create-home spring
COPY --from=build /app/app.jar app.jar
USER spring

# Render Free tiene 512 MB y 0.1 CPU:
# - MaxRAMPercentage limita el heap al 75 % de la memoria del contenedor
# - SerialGC y TieredStopAtLevel=1 reducen consumo y aceleran el arranque en frío
ENV PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
