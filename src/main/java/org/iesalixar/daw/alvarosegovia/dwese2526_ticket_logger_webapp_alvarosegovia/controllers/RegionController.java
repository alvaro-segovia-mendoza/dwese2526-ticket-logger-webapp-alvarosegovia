package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories.RegionRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
    private RegionRepository regionRepository;

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
            Optional<Region> regionOpt = regionRepository.findByIdWithProvinces(id);

            if (regionOpt.isEmpty()) {
                logger.warn("No se encontró la región con ID {}",id);
                String msg = messageSource.getMessage(
                        "msg.region-controller.detail.notFound",
                        null,
                        locale);
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/regions";
            }
            Region region = regionOpt.get();
            // Mappear Entity -> DTO de detalle (incluye provincias)
            RegionDetailDTO regionDTO = RegionMapper.toDetailDTO(region);
            model.addAttribute("region", regionDTO);
            return "views/region/region-detail";
        } catch (Exception e) {
            logger.error("Error al obtener el detalle de la región {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage(
                    "msg.region-controller.detail.error",
                    null,
                    locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/regions";
        }
    }

    /**
     * Muestra el formulario de edición de una región existente.
     * <p>
     * Recupera la región a partir de su identificador, la convierte a un
     * {@link RegionUpdateDTO} y la envía a la vista para permitir su edición.
     * <p>
     * Si la región no existe o se produce un error durante la carga,
     * se registra el incidente y se prepara un mensaje de error
     * internacionalizado.
     *
     * @param id     identificador de la región a editar, recibido como parámetro de la petición
     * @param model  modelo utilizado para enviar los datos a la vista
     * @param locale configuración regional utilizada para la internacionalización de mensajes
     * @return nombre de la plantilla Thymeleaf que renderiza el formulario de edición de regiones
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model, Locale locale) {
        logger.info("Mostrando formulario de edición para la región con ID {}", id);
        Optional<Region> regionOpt;
        RegionUpdateDTO regionDTO = null;
        try {

            // Spring Data: findById devuelve Optional
            regionOpt = regionRepository.findById(id);

            if (regionOpt.isEmpty()) {
                logger.warn("No se encontró la región con ID {}", id);
                String msg = messageSource.getMessage("msg.region.error.notfound",
                        new Object[]{id},
                        locale
                );
                model.addAttribute("errorMessage", msg);
            } else {
                Region region = regionOpt.get();
                regionDTO = RegionMapper.toUpdateDTO(region);
            }

        } catch (Exception e) {
            logger.error("Error al obtener la región con ID {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.region.error.load",
                    null,
                    locale
            );
            model.addAttribute("errorMessage", msg);
        }
        model.addAttribute("region", regionDTO);
        return "views/region/region-form"; // Nombre de la plantilla Thymeleaf para el formulario
    }

    /**
     * Muestra el listado paginado de regiones.
     * <p>
     * Permite recuperar las regiones aplicando paginación y ordenación.
     * Por defecto, se muestran 10 registros por página ordenados por
     * el campo {@code name} en orden ascendente.
     * <p>
     * Los datos se obtienen del repositorio, se convierten a DTOs y se
     * envían a la vista Thymeleaf correspondiente.
     *
     * @param pageable objeto que encapsula la información de paginación
     *                 y ordenación (página, tamaño y criterio de orden)
     * @param model    modelo utilizado para pasar los datos a la vista
     * @return nombre de la vista Thymeleaf que renderiza el listado de regiones
     */
    @GetMapping
    public String listRegions(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            Model model) {
        logger.info("Listando regiones... page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<RegionDTO> listRegionsDTOs =
                    regionRepository.findAll(pageable).map(RegionMapper::toDTO);

            logger.info("Se han cargado {} regiones en la página {}.",
                    listRegionsDTOs.getNumberOfElements(), listRegionsDTOs.getNumber());

            model.addAttribute("page", listRegionsDTOs);

            // Para mantener el sort actual en los enlaces de la vista (sort=campo,asc|desc)
            String sortParam = "name,asc";
            if (listRegionsDTOs.getSort().isSorted()) {
                Sort.Order order = listRegionsDTOs.getSort().iterator().next();
                sortParam = order.getProperty() + "," + order.getDirection().name().toLowerCase();
            }
            model.addAttribute("sortParam", sortParam);
        } catch (Exception e) {
            logger.error("Error al listar las regiones: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al listar las regiones.");
        }

        return "views/region/region-list"; // Nombre de la pantalla Thymeleaf a renderizar
    }


    /**
     * Procesa la inserción de una nueva región.
     * <p>
     * Recibe los datos del formulario mediante un {@link RegionCreateDTO},
     * valida la información introducida y comprueba que no existan conflictos
     * de unicidad en el código o el nombre de la región.
     * <p>
     * En caso de error de validación o de negocio, se redirige al formulario
     * mostrando un mensaje de error internacionalizado. Si la inserción es
     * correcta, se persiste la región y se redirige al listado de regiones.
     *
     * @param regionDTO         DTO que contiene los datos de la nueva región
     *                          recibidos desde el formulario
     * @param result            resultado de la validación del formulario
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                          flash entre redirecciones
     * @param locale            configuración regional utilizada para la
     *                          internacionalización de mensajes
     * @return redirección al listado de regiones o al formulario en caso de error
     */
    @PostMapping("/insert")
    public String insertRegion(@Valid @ModelAttribute("region") RegionCreateDTO regionDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Insertando nueva región con código {}", regionDTO.getCode());

        try {
            // Validación de campos del formulario
            if (result.hasErrors()) {
                return "views/region/region-form";
            }
            // Validación de código duplicado
            if (regionRepository.existsByCode(regionDTO.getCode())) {
                logger.warn("El código de la región {} ya existe.", regionDTO.getCode());
                String errorMessage = messageSource.getMessage("msg.region-controller.insert.codeExist",
                        null,
                        locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/regions/new";
            } else if (regionRepository.existsByName(regionDTO.getName())) {
                logger.warn("El nombre de la región {} ya existe.", regionDTO.getName());
                String errorMessage = messageSource.getMessage("msg.region-controller.insert.nameExist",
                        null,
                        locale
                );
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/regions/new";
            }

            // Mappear DTO -> Entity y persistir
            Region region = RegionMapper.toEntity(regionDTO);
            regionRepository.save(region);
            logger.info("Región {} insertada con éxito.", region.getCode());
        } catch (Exception e) {
            logger.error("Error al insertar la región {}: {}", regionDTO.getCode(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.region-controller.insert.error",
                    null,
                    locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/regions"; // Redirigir a la lista de regiones
    }


    /**
     * Procesa la actualización de una región existente.
     * <p>
     * Recibe los datos del formulario mediante un {@link RegionUpdateDTO},
     * valida la información introducida y comprueba que no existan conflictos
     * de unicidad en el código de la región.
     * <p>
     * Si se producen errores de validación, de negocio o si la región no
     * existe, se redirige al formulario o al listado mostrando los mensajes
     * de error correspondientes. En caso de éxito, la región se actualiza
     * y se redirige al listado de regiones.
     *
     * @param regionDTO          DTO que contiene los datos actualizados de la región
     * @param result             resultado de la validación del formulario
     * @param redirectAttributes atributos utilizados para enviar mensajes flash
     *                           entre redirecciones
     * @param model              modelo utilizado para enviar datos a la vista
     *                           en caso de error
     * @param locale             configuración regional utilizada para la
     *                           internacionalización de mensajes
     * @return redirección al listado de regiones o al formulario en caso de error
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

            // Validar CODE duplicado
            if (regionRepository.existsByCodeAndIdNot(regionDTO.getCode(), regionDTO.getId())) {
                logger.warn("El código de la región {} ya existe para otra región.", regionDTO.getCode());
                String errorMessage = messageSource.getMessage("msg.region-controller.insert.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/regions/edit?id=" + regionDTO.getId();
            }


            // Cargar entidad existente (Spring Data -> Optional)
            Optional<Region> regionOpt = regionRepository.findById(regionDTO.getId());
            if (regionOpt.isEmpty()) {
                logger.warn("No se encontró la región con ID {}", regionDTO.getId());
                String notFound = messageSource.getMessage("msg.region-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", notFound);
                return "redirect:/regions";
            }

            // Mapear DTO -> Entity existente
            Region region = regionOpt.get();
            RegionMapper.copyToExistingEntity(regionDTO, region);

            // Persistir cambios
            regionRepository.save(region);
            logger.info("Región con ID {} actualizada con éxito.", region.getId());

        } catch (Exception e) {
            logger.error("Error al actualizar la región con ID {}: {}", regionDTO.getId(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.region-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/regions";
    }


    /**
     * Procesa la eliminación de una región.
     * <p>
     * Comprueba previamente si la región existe antes de proceder a su
     * eliminación. En caso de que no exista o se produzca un error durante
     * el proceso, se muestra un mensaje de error internacionalizado.
     * <p>
     * Si la eliminación se realiza correctamente, se redirige al listado
     * de regiones.
     *
     * @param id                  identificador de la región a eliminar,
     *                            recibido como parámetro de la petición
     * @param redirectAttributes  atributos utilizados para enviar mensajes
     *                            flash entre redirecciones
     * @param locale              configuración regional utilizada para la
     *                            internacionalización de mensajes
     * @return redirección al listado de regiones
     */
    @PostMapping("/delete")
    public String deleteRegion(@RequestParam("id") Long id,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Eliminando región con id: {}", id);
        try {
            Optional<Region> regionOpt = regionRepository.findById(id);
            if (regionOpt.isEmpty()) {
                logger.warn("No se encontró la región con ID: {}", id);
                String notFound = messageSource.getMessage("msg.region-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", notFound);
                return "redirect:/regions";
            }
            regionRepository.deleteById(id);
            logger.info("Región con ID {} eliminada con éxito.", id);
        } catch (Exception e) {
            logger.error("Error al eliminar la región con ID {}: {}", id, e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.region-controller.delete.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/regions"; // Redirigir a la lista de regiones
    }
}
