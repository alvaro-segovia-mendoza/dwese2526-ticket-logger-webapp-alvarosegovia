#!/bin/sh
# Shebang: indica que el script debe ejecutarse con /bin/sh (shell POSIX-compatible).

set -e
# "set -e": hace que el script termine inmediatamente si cualquier comando devuelve un estado distinto de cero (error).
# Esto evita continuar la ejecución cuando algo falla.

CERT_DIR=/etc/nginx/certs
# Define la variable CERT_DIR con la ruta donde se guardarán los certificados TLS para nginx.

KEY_FILE="$CERT_DIR/server.key"
# Define KEY_FILE con la ruta completa del archivo de clave privada (server.key) dentro de CERT_DIR.

CRT_FILE="$CERT_DIR/server.crt"
# Define CRT_FILE con la ruta completa del certificado (server.crt) dentro de CERT_DIR.

if [ ! -f "$KEY_FILE" ] || [ ! -f "$CRT_FILE" ]; then
# Condicional: comprueba si NO existe KEY_FILE o NO existe CRT_FILE.
# -f comprueba que el archivo existe y es un fichero regular.
# Si falta cualquiera de los dos archivos entramos en el bloque 'then'.

    echo "[nginx] Certificado no encontrado, generando uno autofirmado..."
    # Mensaje informativo por stdout indicando que se generará un certificado autofirmado.

    mkdir -p "$CERT_DIR"
    # Crea el directorio CERT_DIR y todos sus padres si no existen (-p).
    # Esto asegura que la ruta exista antes de intentar escribir los archivos.

    openssl req -x509 -nodes -days 365 \
      -newkey rsa:2048 \
      -keyout "$KEY_FILE" \
      -out "$CRT_FILE" \
      -subj "/C=ES/ST=Andalucia/L=Castilleja de la Cuesta/O=TicketLogger/OU=DAW/CN=localhost"
    # Comando openssl para generar un certificado autofirmado:
    # - req: utilidad de solicitud de certificado (CSR) y generación.
    # - -x509: genera un certificado X.509 directamente (autofirmado) en lugar de una CSR.
    # - -nodes: no encriptar la clave privada (no passphrase), necesario para que nginx lea la clave sin interacción.
    # - -days 365: validez del certificado en días (aquí 1 año).
    # - -newkey rsa:2048: generar una nueva clave RSA de 2048 bits junto con el certificado.
    # - -keyout "$KEY_FILE": ruta donde se guardará la clave privada generada.
    # - -out "$CRT_FILE": ruta donde se guardará el certificado generado.
    # - -subj "...": información del sujeto (DN) para el certificado, evita el prompt interactivo.
    #   Aquí se rellenan campos como país (C), provincia (ST), localidad (L), organización (O),
    #   unidad organizativa (OU) y Common Name (CN). CN=localhost para uso local.
else
# Si ambos archivos existen, se ejecuta el bloque 'else'.

  echo "[nginx] Certificado ya existe, reutilizándolo."
  # Mensaje informativo indicando que no se generará uno nuevo y se reutilizará el existente.
fi
# Fin del condicional if.

echo "[nginx] Arrancando Nginx..."
# Mensaje informativo indicando que a continuación se iniciará nginx.

exec nginx -g "daemon off;"
# Reemplaza el proceso actual del shell por nginx (exec).
# - nginx: ejecuta el binario nginx.
# - -g "daemon off;": pasa una directiva global a nginx para que no se ejecute como demonio (en primer plano).
#   Esto es necesario dentro de contenedores Docker para que nginx quede en primer plano y Docker gestione el PID 1.
# Usar exec evita dejar un proceso shell extra y hace que nginx reciba las señales directamente.
