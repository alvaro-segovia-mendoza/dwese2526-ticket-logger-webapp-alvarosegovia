package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Role;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories.RoleRepository;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories.UserRepository;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.*;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers.UserMapper;
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

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador que maneja las operaciones CRUD para la entidad 'User'.
 * Utiliza 'UserDAO' para interactuar con la base de datos.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    // Logger para registrar eventos importantes en el Controller
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final long PASSWORD_EXPIRY_DAYS = 90;

    // DAO para gestionar las operaciones de las regiones en la base de datos
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MessageSource messageSource;


    /**
     * Muestra el formulario para crear un nuevo usuario.
     *
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla Thymeleaf para el formulario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo usuario.");
        model.addAttribute("user", new UserCreateDTO());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "views/user/user-form";
    }

    /**
     * Muestra el formulario de edición de un usuario existente.
     * <p>
     * Recupera el usuario a partir de su identificador, lo convierte a un
     * {@link UserUpdateDTO} y lo envía a la vista para permitir su edición.
     * <p>
     * Si el usuario no existe o se produce un error durante la carga,
     * se registra el incidente y se prepara un mensaje de error
     * internacionalizado.
     *
     * @param id     identificador del usuario a editar, recibido como parámetro de la petición
     * @param model  modelo utilizado para enviar los datos a la vista
     * @param locale configuración regional utilizada para la internacionalización de mensajes
     * @return nombre de la plantilla Thymeleaf que renderiza el formulario de edición de usuarios
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               Locale locale) {

        logger.info("Mostrando formulario de edición para el usuario con ID {}", id);

        Optional<User> userOpt;
        UserUpdateDTO userDTO = null;

        try {
            // Spring Data: findById devuelve Optional
            userOpt = userRepository.findById(id);

            if (userOpt.isEmpty()) {
                logger.warn("No se encontró el usuario con ID {}", id);
                String msg = messageSource.getMessage(
                        "msg.user-controller.edit.notfound",
                        new Object[]{id},
                        locale
                );
                model.addAttribute("errorMessage", msg);
            } else {
                User user = userOpt.get();
                userDTO = UserMapper.toUpdateDTO(user);
            }

        } catch (Exception e) {
            logger.error("Error al obtener el usuario con ID {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage(
                    "msg.user-controller.edit.error",
                    null,
                    locale
            );
            model.addAttribute("errorMessage", msg);
        }

        model.addAttribute("user", userDTO);
        model.addAttribute("allRoles", roleRepository.findAll());

        return "views/user/user-form";
    }

    /**
     * Muestra el listado paginado de usuarios.
     * <p>
     * Permite recuperar los usuarios aplicando paginación y ordenación.
     * Por defecto, se muestran 10 registros por página ordenados por
     * el campo {@code email} en orden ascendente.
     * <p>
     * Los datos se obtienen del repositorio, se convierten a DTOs y se
     * envían a la vista Thymeleaf correspondiente.
     *
     * @param pageable objeto que encapsula la información de paginación
     *                 y ordenación (página, tamaño y criterio de orden)
     * @param model    modelo utilizado para pasar los datos a la vista
     * @return nombre de la vista Thymeleaf que renderiza el listado de usuarios
     */
    @GetMapping
    public String listUsers(
            @PageableDefault(size = 10, sort = "email", direction = Sort.Direction.ASC) Pageable pageable,
            Model model) {

        logger.info("Listando usuarios... page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            Page<UserDTO> listUsersDTOs =
                    userRepository.findAll(pageable).map(UserMapper::toDTO);

            logger.info("Se han cargado {} usuarios en la página {}.",
                    listUsersDTOs.getNumberOfElements(), listUsersDTOs.getNumber());

            model.addAttribute("page", listUsersDTOs);

            // Mantener el sort actual en los enlaces de la vista (sort=campo,asc|desc)
            String sortParam = "email,asc";
            if (listUsersDTOs.getSort().isSorted()) {
                Sort.Order order = listUsersDTOs.getSort().iterator().next();
                sortParam = order.getProperty() + "," + order.getDirection().name().toLowerCase();
            }
            model.addAttribute("sortParam", sortParam);

        } catch (Exception e) {
            logger.error("Error al listar los usuarios: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error al listar los usuarios.");
        }

        return "views/user/user-list";
    }



    /**
     * Procesa la inserción de un nuevo usuario.
     * <p>
     * Recibe los datos del formulario mediante un {@link UserCreateDTO},
     * valida la información introducida y comprueba que no exista un
     * usuario con el mismo email.
     * <p>
     * En caso de error de validación o de negocio, se redirige al formulario
     * mostrando un mensaje de error internacionalizado. Si la inserción es
     * correcta, se persiste el usuario y se redirige al listado de usuarios.
     *
     * @param userDTO             DTO que contiene los datos del nuevo usuario
     *                            recibidos desde el formulario
     * @param result              resultado de la validación del formulario
     * @param redirectAttributes  atributos utilizados para enviar mensajes
     *                            flash entre redirecciones
     * @param locale              configuración regional utilizada para la
     *                            internacionalización de mensajes
     * @return redirección al listado de usuarios o al formulario en caso de error
     */
    @PostMapping("/insert")
    public String insertUser(@Valid @ModelAttribute("user") UserCreateDTO userDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Insertando nuevo usuario con email {}", userDTO.getEmail());

        try {
            // Validación de campos del formulario
            if (result.hasErrors()) {
                return "views/user/user-form";
            }

            // Validación de email duplicado
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                logger.warn("El email del usuario {} ya existe.", userDTO.getEmail());
                String errorMessage = messageSource.getMessage(
                        "msg.user-controller.insert.emailExist",
                        null,
                        locale
                );
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/new";
            }

            // Inicializar fechas de contraseña
            LocalDateTime lastPasswordChange =
                    userDTO.getLastPasswordChange() != null
                            ? userDTO.getLastPasswordChange()
                            : LocalDateTime.now();

            userDTO.setLastPasswordChange(lastPasswordChange);
            userDTO.setPasswordExpiresAt(lastPasswordChange.plusDays(PASSWORD_EXPIRY_DAYS));

            // Recuperar roles y mapear DTO -> Entity
            Set<Role> roles = new HashSet<>(roleRepository.findByIdIn(userDTO.getRoleIds()));
            User user = UserMapper.toEntity(userDTO, roles);

            // Persistir usuario
            userRepository.save(user);
            logger.info("Usuario {} insertado con éxito.", user.getEmail());

        } catch (Exception e) {
            logger.error("Error al insertar el usuario {}: {}", userDTO.getEmail(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage(
                    "msg.user-controller.insert.error",
                    null,
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users/new";
        }

        return "redirect:/users";
    }

    /**
     * Procesa la actualización de un usuario existente.
     * <p>
     * Recibe los datos del formulario mediante un {@link UserUpdateDTO},
     * valida la información introducida y comprueba que no existan conflictos
     * de unicidad en el email del usuario.
     * <p>
     * Si se producen errores de validación, de negocio o si el usuario no
     * existe, se redirige al formulario o al listado mostrando los mensajes
     * de error correspondientes. En caso de éxito, el usuario se actualiza
     * y se redirige al listado de usuarios.
     *
     * @param userDTO             DTO que contiene los datos actualizados del usuario
     * @param result              resultado de la validación del formulario
     * @param redirectAttributes  atributos utilizados para enviar mensajes flash
     *                            entre redirecciones
     * @param model               modelo utilizado para enviar datos a la vista
     *                            en caso de error
     * @param locale              configuración regional utilizada para la
     *                            internacionalización de mensajes
     * @return redirección al listado de usuarios o al formulario en caso de error
     */
    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") UserUpdateDTO userDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             Locale locale) {

        logger.info("Actualizando usuario con ID {}", userDTO.getId());

        try {
            // Validación de campos
            if (result.hasErrors()) {
                model.addAttribute("allRoles", roleRepository.findAll());
                return "views/user/user-form";
            }

            // Validar EMAIL duplicado
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                logger.warn("El email {} ya existe para otro usuario.", userDTO.getEmail());
                String errorMessage = messageSource.getMessage(
                        "msg.user-controller.update.emailExist",
                        null,
                        locale
                );
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/edit?id=" + userDTO.getId();
            }

            // Cargar usuario existente
            Optional<User> userOpt = userRepository.findById(userDTO.getId());
            if (userOpt.isEmpty()) {
                logger.warn("No se encontró el usuario con ID {}", userDTO.getId());
                String notFound = messageSource.getMessage(
                        "msg.user-controller.detail.notFound",
                        null,
                        locale
                );
                redirectAttributes.addFlashAttribute("errorMessage", notFound);
                return "redirect:/users";
            }

            // Gestionar fechas de contraseña
            LocalDateTime lastPasswordChange =
                    userDTO.getLastPasswordChange() != null
                            ? userDTO.getLastPasswordChange()
                            : LocalDateTime.now();

            userDTO.setLastPasswordChange(lastPasswordChange);
            userDTO.setPasswordExpiresAt(
                    lastPasswordChange.plusDays(PASSWORD_EXPIRY_DAYS)
            );

            // Mapear DTO -> Entity existente
            User user = userOpt.get();
            Set<Role> roles = new HashSet<>(roleRepository.findByIdIn(userDTO.getRoleIds()));
            UserMapper.copyToExistingEntity(userDTO, user, roles);

            // Persistir cambios
            userRepository.save(user);

            logger.info("Usuario con ID {} actualizado con éxito.", user.getId());

        } catch (Exception e) {
            logger.error("Error al actualizar el usuario con ID {}: {}", userDTO.getId(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage(
                    "msg.user-controller.update.error",
                    null,
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/users";
    }




    /**
     * Muestra el detalle de un usuario específico.
     *
     * @param id                 Identificador único del usuario que se desea consultar.
     * @param model              Modelo de Spring MVC utilizado para pasar datos a la vista.
     * @param redirectAttributes Objeto para enviar mensajes flash de error o de información al redirigir.
     * @param locale             Configuración regional actual del usuario (para internacionalización de mensajes).
     * @return El nombre de la plantilla thymeleaf que muestra el detalle del usuario
     *         ({@code views/user/user-detail}), o una redirección a {@code /users} en caso de error.
     */
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Mostrando detalle del usuario con ID {}", id);

        try {
            Optional<User> userOpt = userRepository.findById(id);

            if (userOpt.isEmpty()) {
                logger.warn("No se encontró el usuario con ID {}", id);
                String msg = messageSource.getMessage(
                        "msg.user-controller.detail.notFound",
                        null,
                        locale
                );
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/users";
            }

            User user = userOpt.get();
            UserDetailDTO userDTO = UserMapper.toDetailDTO(user);
            model.addAttribute("user", userDTO);

            return "views/user/user-detail";

        } catch (Exception e) {
            logger.error("Error al obtener el detalle del usuario {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage(
                    "msg.user-controller.detail.error",
                    null,
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/users";
        }
    }



    /**
     * Procesa la eliminación de un usuario.
     * <p>
     * Comprueba previamente si el usuario existe antes de proceder a su
     * eliminación. En caso de que no exista o se produzca un error durante
     * el proceso, se muestra un mensaje de error internacionalizado.
     * <p>
     * Si la eliminación se realiza correctamente, se redirige al listado
     * de usuarios.
     *
     * @param id                 identificador del usuario a eliminar,
     *                           recibido como parámetro de la petición
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           flash entre redirecciones
     * @param locale             configuración regional utilizada para la
     *                           internacionalización de mensajes
     * @return redirección al listado de usuarios
     */
    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Eliminando usuario con ID {}", id);

        try {
            Optional<User> userOpt = userRepository.findById(id);

            if (userOpt.isEmpty()) {
                logger.warn("No se encontró el usuario con ID {}", id);
                String notFound = messageSource.getMessage(
                        "msg.user-controller.detail.notFound",
                        null,
                        locale
                );
                redirectAttributes.addFlashAttribute("errorMessage", notFound);
                return "redirect:/users";
            }

            userRepository.deleteById(id);
            logger.info("Usuario con ID {} eliminado con éxito.", id);

        } catch (Exception e) {
            logger.error("Error al eliminar el usuario con ID {}: {}", id, e.getMessage(), e);
            String errorMessage = messageSource.getMessage(
                    "msg.user-controller.delete.error",
                    null,
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/users";
    }
}
