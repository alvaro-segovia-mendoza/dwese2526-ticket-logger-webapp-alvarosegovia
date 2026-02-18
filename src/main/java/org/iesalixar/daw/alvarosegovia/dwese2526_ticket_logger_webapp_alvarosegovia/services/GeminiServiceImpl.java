package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.services;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.exeptions.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio {@link GeminiService} que utiliza la API externa
 * de Gemini para generar biografías profesionales mediante IA generativa.
 *
 * <p>Esta clase:</p>
 * <ul>
 *     <li>Construye el prompt adecuado.</li>
 *     <li>Realiza la petición HTTP al endpoint de generación de contenido.</li>
 *     <li>Procesa la respuesta JSON recibida.</li>
 *     <li>Gestiona errores de comunicación y los encapsula en {@code ExternalServiceException}.</li>
 * </ul>
 *
 * <p>Las propiedades necesarias se inyectan desde el fichero de configuración:</p>
 * <ul>
 *     <li>{@code gemini.api.key}</li>
 *     <li>{@code gemini.api.baseUrl}</li>
 *     <li>{@code gemini.api.model}</li>
 * </ul>
 *
 * Se apoya en {@link org.springframework.web.client.RestTemplate}
 * para realizar las llamadas HTTP.
 */
@Service
public class GeminiServiceImpl implements GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.baseUrl}")
    private String baseUrl;
    @Value("${gemini.api.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Genera una biografía profesional utilizando la API de Gemini.
     *
     * @param nombre     Nombre completo de la persona.
     * @param intereses  Intereses o habilidades profesionales.
     * @return Biografía generada en texto plano.
     *
     * @throws ExternalServiceException
     *         si ocurre:
     *         <ul>
     *             <li>Error 4xx del cliente</li>
     *             <li>Error 5xx del servidor externo</li>
     *             <li>Problemas de conexión o timeout</li>
     *             <li>Respuesta inválida o sin contenido utilizable</li>
     *         </ul>
     */
    @Override
    public String generateBiography(String nombre, String intereses) {
        String prompt = buildPrompt(nombre, intereses);
        Map<String, Object> body = buildCopy(prompt);

        try {
            Map<String, Object> resp = postGenerateContent(body);

            return extractText(resp).orElseThrow(() ->
                    new ExternalServiceException(
                            "gemini",
                            "generateContent",
                            502,
                            "Gemini no devolvió texto utilizable."
                    )
            );

        } catch (HttpClientErrorException e) {
            throw new ExternalServiceException(
                    "gemini",
                    "generateContent",
                    e.getStatusCode().value(),
                    "Error del cliente al llamar a Gemini: " + e.getStatusCode(),
                    e
            );
        } catch (HttpServerErrorException e) {
            throw new ExternalServiceException(
                    "gemini",
                    "generateContent",
                    e.getStatusCode().value(),
                    "Error del servidor de Gemini: " + e.getStatusCode(),
                    e
            );
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException(
                    "gemini",
                    "generateContent",
                    503,
                    "No se pudo conectar con Gemini (timeout/red).",
                    e
            );
        } catch (Exception e) {
            throw new ExternalServiceException(
                    "gemini",
                    "generateContent",
                    502,
                    "Error inesperado procesando la respuesta de Gemini.",
                    e
            );
        }
    }

    /**
     * Extrae el texto generado desde la estructura JSON devuelta por la API de Gemini.
     *
     * <p>La estructura esperada es:</p>
     * <pre>
     * candidates[0].content.parts[0].text
     * </pre>
     *
     * @param response Mapa que representa la respuesta JSON.
     * @return {@link Optional} con el texto generado si existe,
     *         o vacío si la estructura no es válida.
     */
    private Optional<String> extractText(Map<String, Object> response) {
        if (response == null) return Optional.empty();

        Object candidatesObj = response.get("candidates");
        if (!(candidatesObj instanceof List<?> candidates) || candidates.isEmpty()) return Optional.empty();

        Object firstCandidateObj = candidates.get(0);
        if (!(firstCandidateObj instanceof Map<?, ?> firstCandidate)) return Optional.empty();

        Object contenObj = firstCandidate.get("content");
        if (!(contenObj instanceof Map<?, ?> content)) return Optional.empty();

        Object partsObj = content.get("parts");
        if (!(partsObj instanceof List<?> parts)) return Optional.empty();

        Object firstPartObj = parts.get(0);
        if (!(firstPartObj instanceof Map<?, ?> firstPart)) return Optional.empty();

        Object textObj = firstPart.get("text");
        if (!(textObj instanceof String text)) return Optional.empty();

        return Optional.of(text.trim());
    }

    /**
     * Realiza la petición HTTP POST al endpoint de generación de contenido
     * del modelo configurado en Gemini.
     *
     * @param body Cuerpo de la petición en formato JSON serializable.
     * @return Mapa con la respuesta deserializada.
     *
     * @throws org.springframework.web.client.HttpClientErrorException si ocurre error 4xx.
     * @throws org.springframework.web.client.HttpServerErrorException si ocurre error 5xx.
     * @throws org.springframework.web.client.ResourceAccessException   si ocurre problema de conexión.
     */
    private Map<String, Object> postGenerateContent(Map<String, Object> body) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/models/{model}:generateContent")
                .queryParam("key", apiKey)
                .buildAndExpand(model)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        return response.getBody();
    }

    /**
     * Construye el cuerpo de la petición en el formato requerido por la API de Gemini.
     *
     * @param prompt Texto que se enviará como entrada al modelo.
     * @return Mapa que representa la estructura JSON esperada por el endpoint.
     */
    private Map<String, Object> buildCopy(String prompt) {
        return Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", prompt))
                        )
                )
        );
    }

    /**
     * Construye el prompt que se enviará al modelo generativo.
     *
     * <p>El prompt indica:</p>
     * <ul>
     *     <li>Redacción en tercera persona.</li>
     *     <li>Máximo 200 caracteres.</li>
     *     <li>Sin comillas ni formato Markdown.</li>
     * </ul>
     *
     * @param nombre     Nombre completo de la persona.
     * @param intereses  Intereses o habilidades.
     * @return Cadena formateada lista para enviarse al modelo.
     */
    private String buildPrompt(String nombre, String intereses) {
        return String.format(
                "Escribe una biografía profesional y atractiva en tercera persona para %s. " +
                        "Sus intereses y habilidades son: %s. " +
                        "Máximo 200 caracteres. Sin comillas, sin markdown, sin introducciones.",
                nombre, intereses
        );
    }
}
