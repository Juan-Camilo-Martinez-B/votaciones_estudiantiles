# ==========================================
# 1. Etapa de compilación (Build Stage)
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Instalar bash para compatibilidad con el wrapper de Gradle
RUN apk add --no-cache bash

# Copiar scripts y configuración de Gradle
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Asegurar formato Unix (LF) y permisos de ejecución en gradlew
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Descargar dependencias para aprovechar la caché de capas de Docker
RUN ./gradlew dependencies --no-daemon || true

# Copiar el código fuente del proyecto
COPY src ./src

# Compilar la aplicación y generar el archivo JAR ejecutable
RUN ./gradlew bootJar --no-daemon -x test

# ==========================================
# 2. Etapa de ejecución (Runtime Stage)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear un usuario sin privilegios para mayor seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el archivo JAR empaquetado desde la etapa de compilación
COPY --from=builder /app/build/libs/*.jar app.jar

# Asignar propiedad del archivo JAR al usuario no root
RUN chown appuser:appgroup /app/app.jar

# Usar el usuario sin privilegios
USER appuser

# Exponer el puerto de la aplicación (8080 por defecto en Spring Boot)
EXPOSE 8080

# Ejecutar la aplicación Java con optimizaciones de memoria para contenedores
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
