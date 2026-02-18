package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.services;

/**
 * Servicio encargado de la generación de biografías mediante un proveedor externo de IA.
 * <p>
 * Define el contrato para generar textos descriptivos a partir de un nombre
 * y una lista de intereses o habilidades.
 * </p>
 *
 * La implementación concreta delega la generación del contenido en un servicio
 * externo (por ejemplo, API de Gemini).
 *
 * @author
 */
public interface GeminiService {

    /**
     * Genera una biografía profesional en tercera persona a partir del nombre
     * completo y los intereses indicados.
     *
     * @param nombre     Nombre completo de la persona.
     * @param intereses  Intereses, habilidades o áreas profesionales relevantes.
     * @return Texto generado con la biografía profesional.
     * @throws ExternalServiceException
     *         si ocurre algún error al comunicarse con el servicio externo.
     */
    String generateBiography(String nombre, String intereses);
}
