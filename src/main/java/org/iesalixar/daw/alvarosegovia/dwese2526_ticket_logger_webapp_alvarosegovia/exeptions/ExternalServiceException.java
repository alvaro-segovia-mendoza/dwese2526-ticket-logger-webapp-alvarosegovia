package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.exeptions;

/**
 * Excepción genérica para indicar un fallo al consumir un servicio externo (por ejemplo, una API de IA como Gemini).
 *
 * Se utiliza típicamente en la capa de servicios cuando:
 * - Un proveedor externo responde con error (4xx/5xx),
 * - Hay un timeout o problema de red,
 * - La respuesta no tiene el formato esperado (no se puede extraer el contenido),
 * - O el servicio está temporalmente no disponible.
 *
 * Ejemplos:
 * - "Gemini devuelve 401" (API key inválida)
 * - "Gemini devuelve 429" (rate limit)
 * - "Timeout" al llamar a generateContent
 * - "Respuesta sin candidates/parts/text"
 */
public class ExternalServiceException extends RuntimeException {


    /**
     * Nombre del servicio externo implicado (ej. "gemini", "stripe", "sendgrid").
     */
    private final String service;


    /**
     * Operación/acción realizada contra el servicio (ej. "generateContent", "charge", "sendEmail").
     */
    private final String operation;


    /**
     * Código de estado HTTP si existe (ej. 401, 404, 429, 500). Puede ser null si no aplica (timeout, red, parsing).
     */
    private final Integer status;


    /**
     * Construye una excepción para un fallo en un servicio externo.
     *
     * @param service   nombre del servicio externo (ej. {@code "gemini"}).
     * @param operation operación realizada (ej. {@code "generateContent"}).
     * @param status    código HTTP si se conoce, o {@code null} si no aplica.
     * @param message   mensaje descriptivo del error.
     */
    public ExternalServiceException(String service, String operation, Integer status, String message) {
        super(buildMessage(service, operation, status, message));
        this.service = service;
        this.operation = operation;
        this.status = status;
    }


    /**
     * Construye una excepción para un fallo en un servicio externo incluyendo la causa original (Throwable).
     *
     * @param service   nombre del servicio externo (ej. {@code "gemini"}).
     * @param operation operación realizada (ej. {@code "generateContent"}).
     * @param status    código HTTP si se conoce, o {@code null} si no aplica.
     * @param message   mensaje descriptivo del error.
     * @param cause     excepción original que provocó el fallo (ej. HttpClientErrorException, ResourceAccessException).
     */
    public ExternalServiceException(String service, String operation, Integer status, String message, Throwable cause) {
        super(buildMessage(service, operation, status, message), cause);
        this.service = service;
        this.operation = operation;
        this.status = status;
    }


    /**
     * Nombre del servicio externo.
     */
    public String getService() {
        return service;
    }


    /**
     * Operación realizada contra el servicio.
     */
    public String getOperation() {
        return operation;
    }


    /**
     * Código HTTP del error, si se conoce.
     */
    public Integer getStatus() {
        return status;
    }


    /**
     * Construye un mensaje consistente, estilo "DuplicateResourceException".
     */
    private static String buildMessage(String service, String operation, Integer status, String message) {
        String base = "External service error: " + service + " (" + operation;
        if (status != null) {
            base += ", status=" + status;
        }
        base += ")";
        return (message == null || message.isBlank()) ? base : (base + " - " + message);
    }
}

