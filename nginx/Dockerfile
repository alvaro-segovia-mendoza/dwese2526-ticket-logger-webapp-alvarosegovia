FROM nginx:1.27-alpine
# Usa como imagen base nginx 1.27 sobre Alpine Linux (imagen ligera y rápida).
# Esta imagen ya incluye nginx preinstalado.

RUN apk add --no-cache openssl
# Instala openssl dentro del contenedor usando apk (el gestor de paquetes de Alpine).
# --no-cache evita guardar la caché de paquetes para mantener la imagen pequeña.
# openssl es necesario para generar certificados TLS autofirmados si no existen.

COPY nginx.conf /etc/nginx/nginx.conf
# Copia tu archivo nginx.conf local al contenedor, reemplazando la configuración
# por defecto de Nginx.

COPY docker-entrypoint.sh /docker-entrypoint.sh
# Copia tu script personalizado de arranque (entrypoint) al contenedor.
# Este script es el que genera certificados si no existen y arranca Nginx.

RUN chmod +x /docker-entrypoint.sh
# Le da permisos de ejecución al script entrypoint para poder ejecutarlo dentro del contenedor.

ENTRYPOINT ["/docker-entrypoint.sh"]
# Define el entrypoint del contenedor.
# Esto hace que el contenedor ejecute tu script en lugar de ejecutar nginx directamente.
# Tu script luego generará el certificado si hace falta y finalmente ejecutará nginx.
