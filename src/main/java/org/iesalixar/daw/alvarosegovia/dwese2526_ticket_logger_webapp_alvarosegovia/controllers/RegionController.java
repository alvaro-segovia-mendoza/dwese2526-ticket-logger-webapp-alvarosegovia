package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos.RegionDAO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * Controlador que maneja las operaciones CRUD para la entidad 'Region'.
 * Utiliza 'RegionDAO' para interactuar con la base de datos.
 */
@Controller
@RequestMapping("/regions")
public class RegionController {

    // Logger para registrar eventos importantes en el Controller
    private static final Logger logger = LoggerFactory.getLogger(RegionController.class);

    // DAO para gestionar las operaciones de las regiones en la base de datos
    @Autowired
    private RegionDAO regionDAO;

    @Autowired
    private MessageSource messageSource;

    /**
     * Muestra el formulario para crear una nueva región.
     *
     * @param model Modelo para pasar datos a la vista.
     * @return El nombre de la plantilla Thymeleaf para el formulario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nueva región.");
        model.addAttribute("region", new Region()); // Crear nuevo objeto Region
        return "views/region/region-form"; // Nombre de la plantilla Thymeleaf para el formulario
    }


    /**
     * Muestra el formulario para editar una región existente
     *
     * @param id    ID de la región a editar.
     * @param model Modelo para pasar datos a la vista.
     * @param redirectAttributes Atributos para mensajes flash de redirección.
     * @return El nombre de la plantilla Thymeleaf para el formulario o redirección si no existe.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Mostrando formulario de edición para la región con ID {}", id);
        Region region = null;
        try {
            region = regionDAO.getRegionById(id);
            if (region == null) {
                logger.warn("No se encontró la región con ID {}", id);
            }
            model.addAttribute("region", region); // Añadimos region al modelo
        } catch (Exception e) {
            logger.error("Error al obtener la región con ID {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Error al obtener la región.");
        }
        model.addAttribute("region", region);
        return "views/region/region-form"; // Nombre de la plantilla Thymeleaf para el formulario
    }

    /**
     * Lista todas las regiones y las pasa como atributo al modelo para que sean
     * accesibles en la vista 'region.html'.
     *
     * @param model Objeto del modelo para pasar datos a la vista.
     * @return El nombre de la plantilla Thymeleaf para renderizar la lista de regiones.
     */
    @GetMapping
    public String listRegions(Model model) {
        logger.info("Solicitando la lista de todas las regiones...");
        List<Region> listRegions = null;
        try {
            listRegions = regionDAO.listAllRegions();
            logger.info("Se han cargado {} regiones.",listRegions.size());
        } catch (Exception e) {
            logger.error("Error al listar las regiones: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al listar las regiones.");
        }
        model.addAttribute("listRegions", listRegions); // Pasar la lista de regiones al modelo
        return "views/region/region-list"; //Nombre de la pantalla Thymeleaf a renderizar
    }

    /**
     * Inserta una nueva región en la base de datos.
     *
     * @param region              Objeto que contiene los datos del formulario.
     * @param redirectAttributes  Atributos para mensajes flash de redirección.
     * @return Redirección a la lista de regiones.
     */
    @PostMapping("/insert")
    public String insertRegion(@Valid @ModelAttribute("region") Region region,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Insertando nueva región con código {} y nombre {}", region.getCode(), region.getName());
        try {
            if (result.hasErrors()) {
                return "views/region/region-form";
            }
            if (regionDAO.existsRegionByCode(region.getCode())) {
                logger.warn("El código de la región {} ya existe.", region.getCode());
                String errorMessage = messageSource.getMessage("msg.region-controller.insert.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/regions/new";
            } else if (regionDAO.existsRegionByName(region.getName())) {
                logger.warn("El nombre de la región {} ya existe.", region.getName());
                String errorMessage = messageSource.getMessage("msg.region-controller.insert.nameExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/regions/new";
            }
            regionDAO.insertRegion(region);
            logger.info("Región {} insertada con éxito.", region.getCode());
        } catch (Exception e) {
            logger.error("Error al insertar la región {}: {}", region.getCode(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.region-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/regions"; // Redirigir a la lista de regiones
    }


    /**
     * Actualiza una región existente en la base de datos.
     *
     * @param region              Objeto que contiene los datos del formulario.
     * @param redirectAttributes  Atributos para mensajes flash de redirección.
     * @return Redirección a la lista de regiones.
     */
    @PostMapping("/update")
    public String updateRegion(@Valid @ModelAttribute("region") Region region,
                               BindingResult result,
                               Model model,
                               Locale locale) {
        logger.info("Actualizando región con ID {}", region.getId());
        try {
            if (result.hasErrors()) {
                return "views/region/region-form";
            }
            if (regionDAO.existsRegionByCodeAndNotId(region.getCode(), region.getId())) {
                logger.warn("El código de la región {} ya existe para otra región.", region.getCode());
                String errorMessage = messageSource.getMessage("msg.region-controller.update.codeExist", null, locale);
                model.addAttribute("errorMessage", errorMessage);
                return "views/region/region-form"; // Mantener datos y mostrar error
            }
            if (regionDAO.existsRegionByName(region.getName())) {
                logger.warn("El nombre de la región {} ya existe para otra región.", region.getName());
                String errorMessage = messageSource.getMessage("msg.region-controller.update.nameExist", null, locale);
                model.addAttribute("errorMessage", errorMessage);
                return "views/region/region-form"; // Mantener datos y mostrar error
            }
            regionDAO.updateRegion(region);
            logger.info("Región con ID {} actualizada con éxito.", region.getId());
        } catch (Exception e) {
            logger.error("Error al actualizar la región con ID {}: {}", region.getId(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.region-controller.update.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
            return "views/region/region-form";
        }
        return "redirect:/regions";
    }


    /**
     * Eliminar una región de la base de datos.
     *
     * @param id                 ID de la región a eliminar.
     * @param redirectAttributes Atributos para mensajes flash de redirección.
     * @return Redirección a lista de regiones.
     */
    @PostMapping("/delete")
    public String deleteRegion(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        logger.info("Eliminando región con id: {}", id);
        try {
            regionDAO.deleteRegion(id);
            logger.info("Región con ID {} eliminada con éxito.", id);
        } catch (Exception e) {
            logger.error("Error al eliminar la región con ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la región");
        }
        return "redirect:/regions"; // Redirigir a la lista de regiones
    }
}
