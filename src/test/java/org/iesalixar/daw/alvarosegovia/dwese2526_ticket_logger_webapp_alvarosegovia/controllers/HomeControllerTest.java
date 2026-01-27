package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


/**
 * Tests unitarios (de capa web) para {@link HomeController}.
 * <p>
 * Este test usa {@link WebMvcTest} para arrancar únicamente la infraestructura MVC necesaria:
 * controlador, resolutores, converters, etc. No arranca servicios/repositorios salvo que los declares.
 * </p>
 *
 * <h3>¿Qué validamos aquí?</h3>
 * <ul>
 *   <li>Que la ruta {@code GET /} responde con 200 OK</li>
 *   <li>Que la vista que se resuelve es {@code "index"}</li>
 *   <li>Que el modelo está vacío (porque el controlador no añade atributos)</li>
 * </ul>
 */
@WebMvcTest(HomeController.class) // Indica que solo se testea la capa MVC, cargando HomeController
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad
class HomeControllerTest { // Clase de test (JUnit 5 no requiere public)


    @Autowired
    private MockMvc mockMvc; // MockMvc inyectado por Spring para simular peticiones HTTP


    /**
     * Comprueba el caso más básico:
     * <ul>
     *   <li>Ruta: {@code GET /}</li>
     *   <li>Respuesta: {@code 200 OK}</li>
     *   <li>Vista: {@code "index"}</li>
     * </ul>
     *
     * @throws Exception si falla la ejecución de MockMvc
     */
    @Test // Marca el método como test de JUnit
    @DisplayName("GET / debe devolver 200 y renderizar la vista index") // Nombre legible en el informe de tests
    void home_shouldReturnIndexView() throws Exception { // Método de test (puede lanzar Exception para simplificar)
        mockMvc // Usamos MockMvc para ejecutar una petición HTTP simulada
                .perform(get("/")) // Ejecuta un GET a la ruta raíz "/"
                .andExpect(status().isOk()) // Espera que el status HTTP sea 200
                .andExpect(view().name("index")); // Espera que el nombre lógico de la vista sea "index"
    }
}

