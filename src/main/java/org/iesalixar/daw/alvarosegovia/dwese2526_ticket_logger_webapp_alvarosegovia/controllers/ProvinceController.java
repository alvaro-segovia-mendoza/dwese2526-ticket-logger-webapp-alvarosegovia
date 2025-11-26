package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos.ProvinceDAO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos.RegionDAO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.*;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Province;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Region;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers.ProvinceMapper;
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

import java.util.List;
import java.util.Locale;

/**
 * Controlador que maneja las operaciones CRUD para la entidad 'Province'.
 * Utiliza 'ProvinceDAO' para interactuar con la base de datos.
 */
@Controller
@RequestMapping("/provinces")
public class ProvinceController {

    // Logger para registrar eventos importantes en el Controller
    private static final Logger logger = LoggerFactory.getLogger(ProvinceController.class);

    @Autowired
    private MessageSource messageSource;

    // DAO para gestionar las operaciones de las provincias en la base de datos
    @Autowired
    private ProvinceDAO provinceDAO;

    // DAO para gestionar las operaciones de las regiones en la base de datos
    @Autowired
    private RegionDAO regionDAO;

    /**
     * Muestra el formulario para crear una nueva provincia
     *
     * @param model Modelo para pasar datos a la vista.
     * @return plantilla Thymeleaf para el formulario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model, Locale locale) {
        logger.info("Mostrando formulario para nueva provincia.");
        try {
            List<Region> listRegions = regionDAO.listAllRegions();
            List<RegionDTO> listRegionsDTOs = RegionMapper.toDTOList(listRegions);
            model.addAttribute("province", new ProvinceCreateDTO());
            model.addAttribute("listRegions", listRegionsDTOs);
        } catch (Exception e) {
            logger.error("Error al cargar las regiones para el formulario de provincias: {}", e.getMessage());
            String errorMessage = messageSource.getMessage("msg.province-controller.edit.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "views/province/province-form";
    }

    /**
     * Muestra el formulario para editar una provincia existente.
     *
     * @param id    ID de la provincia a editar.
     * @param model Modelo para pasar datos a la vista.
     * @return plantilla Thymeleaf para el formulario.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model, Locale locale) {
        logger.info("Mostrando formulario de edición para la provincia con ID: {}", id);
        try {
            Province province = provinceDAO.getProvinceById(id);
            ProvinceUpdateDTO provinceDTO = ProvinceMapper.toUpdateDTO(province);
            if (province == null) {
                logger.warn("No se encontró la provincia con ID {}", id);
                String errorMessage = messageSource.getMessage("msg.province-controller.edit.notFound", null, locale);
                model.addAttribute("errorMessage", errorMessage);
            } else {
                List<Region> listRegions = regionDAO.listAllRegions();
                List<RegionDTO> listRegionsDTOs = RegionMapper.toDTOList(listRegions);
                model.addAttribute("province", provinceDTO);
                model.addAttribute("listRegions", listRegionsDTOs);
            }
        } catch (Exception e) {
            logger.error("Error al obtener la provincia con ID {}: {}", id, e.getMessage());
            String errorMessage = messageSource.getMessage("msg.province-controller.edit.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "views/province/province-form";
    }

    /**
     * Muestra el detalle de una provincia específica, incluyendo su región asociada.
     *
     * @param id                  Identificar de la provincia a consultar.
     * @param model               Modelo de Spring MVC para pasar datos a la vista.
     * @param redirectAttributes  Mensajes flash al redirigir (errores/avisos).
     * @param locale              Configuración regional para internacionalización de mensajes.
     * @return Plantilla Thymeleaf {@code views/province/province-detail} o redirección a {@code /provinces}.
     */
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Mostrando detalle de la provincia con ID {}", id);
        try {
            Province province = provinceDAO.getProvinceById(id);
            ProvinceDetailDTO provinceDetailDTO = ProvinceMapper.toDetailDTO(province);
            if (province == null) {
                logger.warn("No se encontró la provincia con ID {}",id);
                String msg = messageSource.getMessage("msg.province-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/provinces";
            }
            model.addAttribute("province", provinceDetailDTO);
            return "views/province/province-detail";
        } catch (Exception e) {
            logger.error("Error al obtener el detalle de la provincia {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.province-controller.detail.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/provinces";
        }
    }

    /**
     * Lista todas las provincias y las pasa al modelo para la vista.
     *
     * @param model  Modelo para pasar datos a la vista.
     * @param page  número de página (0-based)
     * @param size  tamaño de la página (nº de elementos por página)
     * @param sortField campo por el que se ordenan los resultados (id, code, name).
     * @param sortDir dirección de ordenación ("asc" o "desc").
     * @param locale configuración regional (i18n)
     * @return plantilla Thymeleaf par el listado de provincias.
     */
    @GetMapping
    public String listProvinces(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortField", defaultValue = "name") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            Model model,
            Locale locale) {
        logger.info("Listando regiones... page={}, size={},sortField={}, sortDir={}",
                page, size, sortField, sortDir);
        // Evitar valores negativos o tamaños raros
        if (page < 0) page = 0;
        if (size < 0) size = 10;
        try {
            long totalElements = provinceDAO.countProvinces();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            // Ajustar si se pide una página fuera de rango
            if (totalPages > 0 && page >= totalPages) {
                page = totalPages - 1;
            }
            List<Province> entities = provinceDAO.listProvincesPage(page, size, sortField, sortDir);
            List<ProvinceDTO> listProvincesDTOs = ProvinceMapper.toDTOList(entities);
            logger.info("Se han cargado {} provincias.", listProvincesDTOs.size(), page);
            model.addAttribute("listProvinces", listProvincesDTOs);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalElements", totalElements);
            // Para que la vista sepa cómo estamos ordenando ASC/DESC
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");
        } catch (Exception e) {
            logger.error("Error al listar las provincias: {}", e.getMessage());
            String errorMessage = messageSource.getMessage("msg.province-controller.list.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "views/province/province-list";
    }

    /**
     * Inserta una nueva región en la base de datos.
     *
     * @param provinceDTO           Objeto que contiene los datos del formulario.
     * @param result                Resultado de validación.
     * @param redirectAttributes    Atributos para mensajes flash de redirección.
     * @param locale                Configuración regional actual para mensajes.
     * @return Redirección a la lista de provincias.
     */
    @PostMapping("/insert")
    public String insertProvince(@Valid @ModelAttribute("province") ProvinceCreateDTO provinceDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 Locale locale) {
        logger.info("Insertando nueva provincia con código {} y nombre {}", provinceDTO.getCode(), provinceDTO.getName());
        try {
            if (result.hasErrors()) {
                // Volvemos a cargar las regiones para el select cuando hay errores
                List<Region> listRegions = regionDAO.listAllRegions();
                List<RegionDTO> listRegionsDTO = RegionMapper.toDTOList(listRegions);
                model.addAttribute("listRegions", listRegionsDTO);
                return "views/province/province-form";
            }

            if (provinceDAO.existsProvinceByCode(provinceDTO.getCode())) {
                logger.warn("El código de la provincia {} ya existe.", provinceDTO.getCode());
                String errorMessage = messageSource.getMessage("msg.province-controller.insert.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/provinces/new";
            } else if (provinceDAO.existsProvinceByName(provinceDTO.getName())) {
                logger.warn("El nombre de la provincia {} ya existe.", provinceDTO.getName());
                String errorMessage = messageSource.getMessage("msg.province-controller.insert.nameExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/provinces/new";
            }

            // Mappear DTO -> Entity y persistir
            Province province = ProvinceMapper.toEntity(provinceDTO);
            provinceDAO.insertProvince(province);
            logger.info("Provincia {} insertada con éxito.", province.getCode());
        } catch (Exception e) {
            logger.error("Error al insertar la provincia {}: {}", provinceDTO.getCode(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.province-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/provinces"; // Redirigir a la lista de provincias
    }

    /**
     * Actualiza una provincia existente en la base de datos.
     * @param provinceDTO           Provincia con los datos actualizados.
     * @param result             Resultado de la validación.
     * @param redirectAttributes Atributos para mensajes flash.
     * @param locale             Configuración regional actual.
     * @return Redirección al listado de provincias.
     */
    @PostMapping("/update")
    public String updateProvince(@Valid @ModelAttribute("province") ProvinceUpdateDTO provinceDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 Locale locale) {
        logger.info("Actualizando provincia con ID {}", provinceDTO.getId());
        try {
            if (result.hasErrors()) {
                // Volvemos a cargar las regiones para el select cuando hay errores
                List<Region> listRegions = regionDAO.listAllRegions();
                List<RegionDTO> listRegionsDTOs = RegionMapper.toDTOList(listRegions);
                model.addAttribute("listRegions", listRegionsDTOs);
                return "views/province/province-form";
            }

            if (provinceDAO.existsProvinceByCodeAndNotId(provinceDTO.getCode(), provinceDTO.getId())) {
                logger.warn("El código de la provincia {} ya existe para otra provincia.",provinceDTO.getCode());
                String errorMessage = messageSource.getMessage("msg.province-controller.update.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/provinces/edit?id=" + provinceDTO.getId();
            }
            if (provinceDAO.existsProvinceByName(provinceDTO.getName())) {
                logger.warn("El nombre de la provincia {} ya existe para otra provincia.", provinceDTO.getName());
                String errorMessage = messageSource.getMessage("msg.province-controller.update.nameExist", null, locale);
                model.addAttribute("errorMessage", errorMessage);
                return "views/province/province-form"; // Mantener datos y mostrar error
            }

            // Mappear DTO -> Entity y persistir
            Province province = ProvinceMapper.toEntity(provinceDTO);
            provinceDAO.updateProvince(province);
            logger.info("Provincia con ID {} actualizada con éxito.", province.getId());

        } catch (Exception e) {
            logger.error("Error al actualizar la provincia con ID {}: {}", provinceDTO.getId(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.province-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/provinces";
    }

    @PostMapping("/delete")
    public String deleteProvince(@RequestParam("id") Long id,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {
        logger.info("Eliminando provincia con ID {}", id);
        try {
            provinceDAO.deleteProvince(id);
            logger.info("Provincia con ID {} eliminada con éxito.", id);
        } catch (Exception e) {
            logger.error("Error al eliminar la provincia con ID {}: {}", id, e.getMessage());
            String errorMessage = messageSource.getMessage("msg.province-controller.delete.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/provinces";
    }
}
