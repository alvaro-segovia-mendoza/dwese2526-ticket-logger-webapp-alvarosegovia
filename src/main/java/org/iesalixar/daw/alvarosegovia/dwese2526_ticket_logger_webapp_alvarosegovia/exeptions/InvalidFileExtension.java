package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.exeptions;

public class InvalidFileExtension extends RuntimeException {
    /**
     * Recurso/funcionalidad afectada por el fichero (por ejemplo: "userProfile", "attachement", "ticket").
     * Mantener este dato ayuda a homogeneizar el manejo de errores entre dominios.
     */
    private final String resource;

    /**
     * Nombre del campo del formulario asociado al fichero (por ejemplo: "profileImageFile").
     */
    private final String field;

    /**
     * Valor o detalle relevante asociado al error (por ejemplo: "image/png", "3.2MB", "empty", etc.).
     * Pueder ser {@code Null} si no aplica.
     */
    private final Object value;

    /**
     * Construye la exepción indicando al contexto (recurso/campo/valor) del fichero inválido.
     * @param resource nombre del recurso/funcionalidad (ej. {@code "userProfile"}).
     * @param field    nombre del campo del formulario (ej. {@code "profileImageFile"}).
     * @param value    valor/detalle asociado al error (ej. {@code "application/pdf"} o {@code 5242880}).
     */
    public InvalidFileExtension(String resource, String field, Object value) {
        super("Invalid file for " + resource + " (" + field + "=" + value + ")");
        this.resource = resource;
        this.field = field;
        this.value = value;
    }

    /**
     * Construye la exepción permitiendo añadir un detalle adicional en el mensaje.
     * Útil para fallos de guardado en disco, permisos, rutas inválidas, etc.
     * @param resource nombre del recurso/funcionalidad (ej. {@code "userProfile"}).
     * @param field    nombre del campo del formulario (ej. {@code "profileImageFile"}).
     * @param value    valor/detalle asociado al error (ej. {@code "application/pdf"} o {@code 5242880}).
     * @param detail   detalle adicional legible (ej. {@code "File too large"}).
     */
    public InvalidFileExtension(String resource, String field, Object value, String detail) {
        super("Invalid file for " + resource + " (" + field + "=" + value + "): " + detail);
        this.resource = resource;
        this.field = field;
        this.value = value;
    }
}

