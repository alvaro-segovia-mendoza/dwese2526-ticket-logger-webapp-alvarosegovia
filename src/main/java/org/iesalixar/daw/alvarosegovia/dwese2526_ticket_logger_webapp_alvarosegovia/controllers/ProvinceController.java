package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories.ProvinceRepository;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories.RegionRepository;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.*;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Province;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Region;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers.ProvinceMapper;
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
    private ProvinceRepository provinceRepository;

    // DAO para gestionar las operaciones de las regiones en la base de datos
    @Autowired
    private RegionRepository regionRepository;

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
            List<Region> listRegions = regionRepository.findAll();
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
     * Muestra el formulario de edición de una provincia existente.
     * <p>
     * Recupera la provincia a partir de su identificador, la convierte a un
     * {@link ProvinceUpdateDTO} y la envía a la vista junto con la lista de
     * regiones disponibles para permitir su edición.
     * <p>
     * Si la provincia no existe o se produce un error durante la carga,
     * se registra el incidente y se prepara un mensaje de error
     * internacionalizado.
     *
     * @param id     identificador de la provincia a editar, recibido como parámetro de la petición
     * @param model  modelo utilizado para enviar los datos a la vista
     * @param locale configuración regional utilizada para la internacionalización de mensajes
     * @return nombre de la plantilla Thymeleaf que renderiza el formulario de edición de provincias
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model, Locale locale) {
        logger.info("Mostrando formulario de edición para la provincia con ID {}", id);
        Optional<Province> provinceOpt;
        ProvinceUpdateDTO provinceDTO = null;
        try {

            // Spring Data: findById devuelve Optional
            provinceOpt = provinceRepository.findById(id);

            if (provinceOpt.isEmpty()) {
                logger.warn("No se encontró la provincia con ID {}", id);
                String msg = messageSource.getMessage(
                        "msg.province-controller.edit.notFound",
                        new Object[]{id},
                        locale
                );
                model.addAttribute("errorMessage", msg);
            } else {
                Province province = provinceOpt.get();
                provinceDTO = ProvinceMapper.toUpdateDTO(province);

                List<Region> regions = regionRepository.findAll();
                List<RegionDTO> regionsDTO = RegionMapper.toDTOList(regions);
                model.addAttribute("listRegions", regionsDTO);
            }

        } catch (Exception e) {
            logger.error("Error al obtener la provincia con ID {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage(
                    "msg.province-controller.edit.error",
                    null,
                    locale
            );
            model.addAttribute("errorMessage", msg);
        }

        model.addAttribute("province", provinceDTO);
        return "views/province/province-form";
    }


    /**
     * Muestra el detalle de una provincia específica, incluyendo su región asociada.
     *
     * @param id                 Identificador único de la provincia a consultar.
     * @param model              Modelo de Spring MVC utilizado para pasar datos a la vista.
     * @param redirectAttributes Objeto para enviar mensajes flash de error o de información al redirigir.
     * @param locale             Configuración regional actual del usuario (para internacionalización de mensajes).
     * @return El nombre de la plantilla Thymeleaf que muestra el detalle de la provincia
     *         ({@code views/province/province-detail}), o una redirección a {@code /provinces} en caso de error.
     */
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Mostrando detalle de la provincia con ID {}", id);
        try {
            // Cargar la provincia junto con su región asociada (fetch) para evitar LazyInitializationException
            Optional<Province> provinceOpt = provinceRepository.findByIdWithRegion(id);

            if (provinceOpt.isEmpty()) {
                logger.warn("No se encontró la provincia con ID {}", id);
                String msg = messageSource.getMessage(
                        "msg.province-controller.detail.notFound",
                        null,
                        locale
                );
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/provinces";
            }
            Province province = provinceOpt.get();
            // Mappear Entity -> DTO de detalle (incluye región)
            ProvinceDetailDTO provinceDTO = ProvinceMapper.toDetailDTO(province);
            model.addAttribute("province", provinceDTO);
            return "views/province/province-detail";
        } catch (Exception e) {
            logger.error("Error al obtener el detalle de la provincia {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage(
                    "msg.province-controller.detail.error",
                    null,
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/provinces";
        }
    }


    /**
     * Lista todas las provincias y las pasa al modelo para la vista.
     *
     * @param model  Modelo para pasar datos a la vista.
     * @return plantilla Thymeleaf par el listado de provincias.
     */
    @GetMapping
    public String listProvinces(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            Model model) {
        logger.info("Listando provincias... page={}, size={},sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<ProvinceDTO> listProvincesDTOs =
                    provinceRepository.findAll(pageable).map(ProvinceMapper::toDTO);

            logger.info("Se han cargado {} provincias en la página {}.",
                    listProvincesDTOs.getNumberOfElements(),listProvincesDTOs.getNumber());
            model.addAttribute("page", listProvincesDTOs);

            // Para mantener el sort actual en los enlaces de la vista (sort=campo,asc|desc)
            String sortParam = "name,asc";
            if (listProvincesDTOs.getSort().isSorted()) {
                Sort.Order order = listProvincesDTOs.getSort().iterator().next();
                sortParam = order.getProperty() + "," + order.getDirection().name().toLowerCase();
            }
            model.addAttribute("sortParam", sortParam);
        } catch (Exception e) {
            logger.error("Error al listar las provincias: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al listar las provincias.");
        }

        return "views/province/province-list"; // Nombre de la pantalla Thymeleaf a renderizar
    }

    /**
     * Procesa la inserción de una nueva provincia.
     * <p>
     * Recibe los datos del formulario mediante un {@link ProvinceCreateDTO},
     * valida la información introducida y comprueba que no existan conflictos
     * de unicidad en el código o el nombre de la provincia.
     * <p>
     * En caso de error de validación o de negocio, se redirige al formulario
     * mostrando un mensaje de error internacionalizado. Si la inserción es
     * correcta, se persiste la provincia y se redirige al listado de provincias.
     *
     * @param provinceDTO        DTO que contiene los datos de la nueva provincia
     *                           recibidos desde el formulario
     * @param result             resultado de la validación del formulario
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           flash entre redirecciones
     * @param locale             configuración regional utilizada para la
     *                           internacionalización de mensajes
     * @return redirección al listado de provincias o al formulario en caso de error
     */
    @PostMapping("/insert")
    public String insertProvince(@Valid @ModelAttribute("province") ProvinceCreateDTO provinceDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {
        logger.info("Insertando nueva provincia con código {}", provinceDTO.getCode());

        try {
            // Validación de campos del formulario
            if (result.hasErrors()) {
                return "views/province/province-form";
            }
            // Validación de código duplicado
            if (provinceRepository.existsByCode(provinceDTO.getCode())) {
                logger.warn("El código de la provincia {} ya existe.", provinceDTO.getCode());
                String msg = messageSource.getMessage(
                        "msg.province-controller.insert.codeExist",
                        null,
                        locale
                );
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/provinces/new";
            } else if (provinceRepository.existsByName(provinceDTO.getName())) {
                logger.warn("El nombre de la provincia {} ya existe.", provinceDTO.getName());
                String msg = messageSource.getMessage(
                        "msg.province-controller.insert.nameExist",
                        null,
                        locale
                );
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/provinces/new";
            }

            // Mapear DTO -> Entity y persistir
            Province province = ProvinceMapper.toEntity(provinceDTO);
            provinceRepository.save(province);
            logger.info("Provincia {} insertada con éxito.", province.getCode());
        } catch (Exception e) {
            logger.error("Error al insertar la provincia {}: {}", provinceDTO.getCode(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage(
                    "msg.province-controller.insert.error",
                    null,
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/provinces";
    }


    /**
     * Procesa la actualización de una provincia existente.
     * <p>
     * Recibe los datos del formulario mediante un {@link ProvinceUpdateDTO},
     * valida la información introducida y comprueba que no existan conflictos
     * de unicidad en el código o el nombre de la provincia.
     * <p>
     * Si se producen errores de validación, de negocio o si la provincia no
     * existe, se redirige al formulario o al listado mostrando los mensajes
     * de error correspondientes. En caso de éxito, la provincia se actualiza
     * y se redirige al listado de provincias.
     *
     * @param provinceDTO        DTO que contiene los datos actualizados de la provincia
     * @param result             resultado de la validación del formulario
     * @param redirectAttributes atributos utilizados para enviar mensajes flash
     *                           entre redirecciones
     * @param locale             configuración regional utilizada para la
     *                           internacionalización de mensajes
     * @return redirección al listado de provincias o al formulario en caso de error
     */
    @PostMapping("/update")
    public String updateProvince(@Valid @ModelAttribute("province") ProvinceUpdateDTO provinceDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {

        logger.info("Actualizando provincia con ID {}", provinceDTO.getId());

        try {
            // --- Validación de campos del formulario ---
            if (result.hasErrors()) {
                return "views/province/province-form";
            }

            // --- Validación de código duplicado ---
            if (provinceRepository.existsByCodeAndIdNot(provinceDTO.getCode(), provinceDTO.getId())) {
                logger.warn("El código de la provincia {} ya existe para otra provincia.", provinceDTO.getCode());
                String msg = messageSource.getMessage("msg.province-controller.insert.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/provinces/edit?id=" + provinceDTO.getId();
            }

            // --- Validación de nombre duplicado ---
            if (provinceRepository.existsByName(provinceDTO.getName())) {
                logger.warn("El nombre de la provincia {} ya existe para otra provincia.", provinceDTO.getName());
                String msg = messageSource.getMessage("msg.province-controller.insert.nameExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/provinces/edit?id=" + provinceDTO.getId();
            }

            // Cargar entidad existente (Spring Data -> Optional)
            Optional<Province> provinceOpt = provinceRepository.findById(provinceDTO.getId());
            if (provinceOpt.isEmpty()) {
                logger.warn("No se encontró la provincia con ID {}", provinceDTO.getId());
                String msg = messageSource.getMessage("msg.province-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/provinces";
            }

            Province province = provinceOpt.get();
            ProvinceMapper.copyToExistingEntity(provinceDTO, province);
            provinceRepository.save(province);

            logger.info("Provincia con ID {} actualizada con éxito.", province.getId());

        } catch (Exception e) {
            logger.error("Error al actualizar la provincia con ID {}: {}", provinceDTO.getId(), e.getMessage(), e);
            String msg = messageSource.getMessage("msg.province-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
        }

        return "redirect:/provinces";
    }

    /**
     * Método que gestiona la eliminación de una provincia.
     * <p>
     * Este método se invoca mediante una petición POST a la URL "/delete".
     * Intenta eliminar la provincia con el ID proporcionado.
     * En caso de éxito, redirige a la lista de provincias.
     * Si ocurre un error durante la eliminación, se captura la excepción,
     * se registra el error y se añade un mensaje de error a los atributos de redirección.
     * </p>
     *
     * @param id                 el identificador de la provincia a eliminar
     * @param redirectAttributes contenedor para añadir atributos flash que estarán disponibles
     *                           tras la redirección (como mensajes de error)
     * @param locale             el locale actual de la petición, utilizado para obtener
     *                           mensajes localizados
     * @return                   una redirección a la vista "/provinces"
     */
    @PostMapping("/delete")
    public String deleteProvince(@RequestParam("id") Long id,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {
        logger.info("Eliminando provincia con ID {}", id);
        try {
            Optional<Province> provinceOpt = provinceRepository.findById(id);
            if (provinceOpt.isEmpty()) {
                logger.warn("No se encontró la provincia con ID: {}", id);
                String notFound = messageSource.getMessage("msg.province-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", notFound);
                return "redirect:/provinces";
            }
            provinceRepository.deleteById(id);
            logger.info("Provincia con ID {} eliminada con éxito.", id);
        } catch (Exception e) {
            logger.error("Error al eliminar la provincia con ID {}: {}", id, e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.province-controller.delete.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/provinces";
    }
}
