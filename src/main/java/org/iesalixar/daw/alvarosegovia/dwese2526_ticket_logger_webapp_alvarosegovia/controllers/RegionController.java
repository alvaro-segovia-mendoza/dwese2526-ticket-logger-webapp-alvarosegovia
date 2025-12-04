package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import jakarta.validation.Valid;
import org.apache.coyote.Request;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos.RegionDAO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.RegionCreateDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.RegionDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.RegionDetailDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.RegionUpdateDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Region;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers.RegionMapper;
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



    asdasdasdasd


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
        model.addAttribute("region", new RegionCreateDTO()); // Crear nuevo objeto Region
        return "views/region/region-form"; // Nombre de la plantilla Thymeleaf para el formulario
    }

    /**
     *  Muestra el detalle de una región específica, incluyendo su lista de provincias asociadas.
     *
     * @param id                 Identificador único de la región que se desea consultar.
     * @param model              Modelo de Spring MVC utilizado para pasar datos a la vista.
     * @param redirectAttributes Objeto para enviar mensajes flash de error o de información al redirigir.
     * @param locale             Configuración regional actual del usuario (para internacionalización de mensajes).
     * @return El nombre de la plantilla thymeleaf que muestra el detalle de la región
     *          ({@code views/region/region-detail}), o una redirección a {@code /regions} en caso de error.
     */
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Mostrando detalle de la región con ID {}", id);
        try {
            // Cargar la región junto con sus provincias (fetch) para evitar LazyInitializationExeption
            Region region = regionDAO.getRegionById(id);
            if (region == null) {
                String msg = messageSource.getMessage("msg.region-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/regions";
            }
            // Mappear Entity -> DTO de detalle (incluye provincias)
            RegionDetailDTO regionDTO = RegionMapper.toDetailDTO(region);
            model.addAttribute("region", regionDTO);
            return "views/region/region-detail";
        } catch (Exception e) {
            logger.error("Error al obtener el detalle de la región {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.region-controller.detail.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/regions";
        }
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
        RegionUpdateDTO regionDTO = null;
        try {
            region = regionDAO.getRegionById(id);
            if (region == null) {
                logger.warn("No se encontró la región con ID {}", id);
            }
            regionDTO = RegionMapper.toUpdateDTO(region);
        } catch (Exception e) {
            logger.error("Error al obtener la región con ID {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Error al obtener la región.");
        }
        model.addAttribute("region", regionDTO);
        return "views/region/region-form"; // Nombre de la plantilla Thymeleaf para el formulario
    }

    /**
     * Lista todas las regiones y las pasa como atributo al modelo para que sean
     * accesibles en la vista 'region.html'.
     *
     * @param page  número de página (0-based)
     * @param size  tamaño de la página (nº de elementos por página)
     * @param sortField campo por el que se ordenan los resultados (id, code, name).
     * @param sortDir dirección de ordenación ("asc" o "desc").
     * @param model Objeto del modelo para pasar datos a la vista.
     * @return El nombre de la plantilla Thymeleaf para renderizar la lista de regiones.
     */
    @GetMapping
    public String listRegions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortField", defaultValue = "name") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            Model model) {
        logger.info("Listando regiones... page={}, size={},sortField={}, sortDir={}",
                page, size, sortField, sortDir);
        // Evitar valores negativos o tamaños raros
        if (page < 0) page = 0;
        if (size < 0) size = 10;

        try {
            long totalElements = regionDAO.countRegions();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            // Si se pide una página fuera de rango, ajustamos
            if (totalPages > 0 && page >= totalPages) {
                page = totalPages - 1;
            }
            List<Region> listRegions = regionDAO.listRegionsPage(page, size, sortField, sortDir);
            List<RegionDTO> listRegionsDTOs = RegionMapper.toDTOList(listRegions);
            logger.info("Se han cargado {} regiones.", listRegionsDTOs.size(), page);
            model.addAttribute("listRegions", listRegionsDTOs);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalElements", totalElements);
            // Para que la vista sepa cómo estamos ordenando ASC/DESC
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");
        } catch (Exception e) {
            logger.error("Error al listar las regiones: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al listar las regiones.");
        }
        return "views/region/region-list"; //Nombre de la pantalla Thymeleaf a renderizar
    }

    /**
     * Inserta una nueva región en la base de datos.
     *
     * @param regionDTO              Objeto que contiene los datos del formulario.
     * @param redirectAttributes  Atributos para mensajes flash de redirección.
     * @return Redirección a la lista de regiones.
     */
    @PostMapping("/insert")
    public String insertRegion(@Valid @ModelAttribute("region") RegionCreateDTO regionDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Insertando nueva región con código {} y nombre {}", regionDTO.getCode(), regionDTO.getName());

        try {
            if (result.hasErrors()) {
                return "views/region/region-form";
            }
            if (regionDAO.existsRegionByCode(regionDTO.getCode())) {
                logger.warn("El código de la región {} ya existe.", regionDTO.getCode());
                String errorMessage = messageSource.getMessage("msg.region-controller.insert.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/regions/new";
            } else if (regionDAO.existsRegionByName(regionDTO.getName())) {
                logger.warn("El nombre de la región {} ya existe.", regionDTO.getName());
                String errorMessage = messageSource.getMessage("msg.region-controller.insert.nameExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/regions/new";
            }
            // Mappear DTO -> Entity y persistir
            Region region = RegionMapper.toEntity(regionDTO);
            regionDAO.insertRegion(region);
            logger.info("Región {} insertada con éxito.", region.getCode());
        } catch (Exception e) {
            logger.error("Error al insertar la región {}: {}", regionDTO.getCode(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.region-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/regions"; // Redirigir a la lista de regiones
    }


    /**
     * Actualiza una región existente en la base de datos.
     *
     * @param regionDTO              Objeto que contiene los datos del formulario.
     * @param redirectAttributes  Atributos para mensajes flash de redirección.
     * @param result              Resultado de la validación del formulario.
     * @param locale              Objeto que representa la configuración regional actual del usuario,
     *                            utilizado para obtener los mensajes traducidos desde {@code messageSource}.
     * @return Redirección a la lista de regiones.
     */
    @PostMapping("/update")
    public String updateRegion(@Valid @ModelAttribute("region") RegionUpdateDTO regionDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model,
                               Locale locale) {

        logger.info("Actualizando región con ID {}", regionDTO.getId());

        try {
            // Validación de campos
            if (result.hasErrors()) {
                return "views/region/region-form";
            }

            // Validar CODE duplicado (mismo mensaje que insert)
            if (regionDAO.existsRegionByCodeAndNotId(regionDTO.getCode(), regionDTO.getId())) {
                logger.warn("El código de la región {} ya existe para otra región.", regionDTO.getCode());

                String errorMessage = messageSource.getMessage(
                        "msg.region-controller.insert.codeExist",  // MISMO MENSAJE QUE INSERT
                        null,
                        locale
                );

                model.addAttribute("errorMessage", errorMessage);
                return "views/region/region-form"; // Mantiene datos y muestra error
            }

            // Validar NAME duplicado (igual que insert)
            if (regionDAO.existsRegionByName(regionDTO.getName())) {
                logger.warn("El nombre de la región {} ya existe para otra región.", regionDTO.getName());

                String errorMessage = messageSource.getMessage(
                        "msg.region-controller.insert.nameExist",
                        null,
                        locale
                );

                model.addAttribute("errorMessage", errorMessage);
                return "views/region/region-form";
            }

            // Cargar entidad y actualizar
            Region region = regionDAO.getRegionById(regionDTO.getId());
            if (region == null) {
                logger.warn("No se encontró la región con ID {}", regionDTO.getId());
                String notFound = messageSource.getMessage("msg.region-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", notFound);
                return "redirect:/regions";
            }

            RegionMapper.copyToExistingEntity(regionDTO, region);
            regionDAO.updateRegion(region);
            logger.info("Región con ID {} actualizada con éxito.", region.getId());

        } catch (Exception e) {
            logger.error("Error al actualizar la región con ID {}: {}", regionDTO.getId(), e.getMessage());

            String errorMessage = messageSource.getMessage(
                    "msg.region-controller.update.error",
                    null,
                    locale
            );

            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/regions";
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
