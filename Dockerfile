FROM maven:3.9-eclipse-temurin-21-alpine AS builder
# Stage 1: Build
# Usa una imagen ligera con Maven y JDK 21 sobre Alpine Linux.
# Esta etapa sirve solo para compilar la aplicación y generar el JAR.
# 'AS builder' le da un nombre a esta etapa para poder copiar archivos desde aquí en el siguiente stage.

WORKDIR /app
# Directorio de trabajo dentro del contenedor donde se ejecutarán los comandos de Maven.

COPY pom.xml .
# Copia únicamente el archivo pom.xml primero.
# Esto permite aprovechar la cache de Docker: si el pom.xml no cambia, no se vuelven a descargar dependencias.

RUN mvn -q -e -B dependency:go-offline
# Pre-descarga todas las dependencias declaradas en pom.xml sin compilar nada.
# -q: modo silencioso
# -e: muestra errores completos si falla
# -B: batch mode (sin interacción)
# Esto acelera los builds posteriores y evita descargar dependencias cada vez.

COPY src ./src
# Copia todo el código fuente al contenedor (la carpeta src).

RUN mvn -q -e -B clean package -DskipTests
# Compila y empaqueta la aplicación generando el JAR.
# -DskipTests evita ejecutar los tests para acelerar la compilación.

FROM eclipse-temurin:21-jre-jammy
# Stage 2: Runtime
# Imagen base solo con JRE 21 sobre Ubuntu Jammy.
# Más ligera que la imagen de Maven, ideal para producción.
# Aquí no necesitamos Maven, solo ejecutar el JAR generado.

RUN useradd -ms /bin/bash spring
# Crea un usuario sin privilegios llamado 'spring' para ejecutar la app.
# Mejora la seguridad al no ejecutar la app como root.

WORKDIR /app
# Directorio de trabajo donde se ejecutará la aplicación.

COPY --from=builder /app/target/*.jar app.jar
# Copia el JAR generado en el stage de build (builder) al stage de runtime.
# Esto es lo que hace que la imagen final solo contenga el JAR y JRE, sin Maven ni código fuente.

RUN mkdir -p /app && chown -R spring /app
# Asegura que el directorio /app exista y sea propiedad del usuario 'spring'.
# Esto permite que la app pueda escribir en /app si es necesario (por ejemplo uploads).

USER spring
# Cambia a usuario 'spring' para ejecutar la aplicación de forma segura, sin privilegios de root.

EXPOSE 8080
# Expone el puerto 8080 del contenedor, el mismo que la app Spring Boot usará para escuchar peticiones.

ENV JAVA_OPTS="-Xms256m -Xmx512m"
# Define variables de entorno para la JVM:
# -Xms256m: memoria inicial 256 MB
# -Xmx512m: memoria máxima 512 MB

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
# Define el comando que se ejecutará al iniciar el contenedor.
# 'sh -c' permite expandir la variable JAVA_OPTS antes de ejecutar el JAR.
