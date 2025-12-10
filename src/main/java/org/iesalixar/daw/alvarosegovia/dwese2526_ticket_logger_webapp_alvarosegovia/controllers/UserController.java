package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos.RoleDAO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos.UserDAO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.*;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Region;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Role;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers.RegionMapper;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

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
    private UserDAO userDAO;

    @Autowired
    private RoleDAO roleDAO;

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
        model.addAttribute("allRoles", roleDAO.listAllRoles());
        return "views/user/user-form";
    }

    /**
     * Muestra el formulario para editar un usuario existente
     *
     * @param id    ID del usuario a editar.
     * @param model Modelo para pasar datos a la vista.
     * @param redirectAttributes Atributos para mensajes flash de redirección.
     * @return El nombre de la plantilla Thymeleaf para el formulario o redirección si no existe.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model, Locale locale) {
        logger.info("Mostrando formulario de edición para la región con ID {}", id);

        try {
            User user = userDAO.getUserById(id);
            UserUpdateDTO userDTO =  UserMapper.toUpdateDTO(user);
            if (userDTO == null) {
                logger.warn("No se encontró el usuario con ID {}", id);
                String errorMessage = messageSource.getMessage("msg.user-controller.edit.notfound", null, locale);
                model.addAttribute("errorMessage", errorMessage);
                model.addAttribute("user", new UserUpdateDTO());
            } else {
                model.addAttribute("user", userDTO);
            }
        } catch (Exception e) {
            logger.error("Error al obtener el usuario con ID {}: {}", id, e.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.edit.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("user", new UserUpdateDTO());
        }
        model.addAttribute("allRoles", roleDAO.listAllRoles());
        return "views/user/user-form"; // Nombre de la plantilla Thymeleaf para el formulario
    }

    /**
     * Lista todos los usuarios y los pasa como atributo al modelo para que sean
     * accesibles en la vista 'user.html'.
     *
     * @param page  número de página (0-based)
     * @param size  tamaño de la página (nº de elementos por página)
     * @param sortField campo por el que se ordenan los resultados (todos los campos).
     * @param sortDir dirección de ordenación ("asc" o "desc").
     * @param model Objeto del modelo para pasar datos a la vista.
     * @return El nombre de la plantilla Thymeleaf para renderizar la lista de usuarios.
     */
    @GetMapping
    public String listUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortField", defaultValue = "email") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            Model model) {

        logger.info("Listando usuarios... page={}, size={}, sortField={}, sortDir={}", page, size, sortField, sortDir);

        // Evitar valores negativos o tamaños inválidos
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        try {
            long totalElements = userDAO.countUsers();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            // Ajustar si la página solicitada está fuera de rango
            if (totalPages > 0 && page >= totalPages) {
                page = totalPages - 1;
            }

            List<User> users = userDAO.listUsersPage(page, size, sortField, sortDir);
            List<UserDTO> userDTOs = UserMapper.toDTOList(users);

            logger.info("Se han cargado {} usuarios en la página {}.", userDTOs.size(), page);

            model.addAttribute("listUsers", userDTOs);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalElements", totalElements);

            // Para que la vista sepa cómo invertir el orden ASC/DESC
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");

        } catch (Exception e) {
            logger.error("Error al listar los usuarios: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error al listar los usuarios.");
        }

        return "views/user/user-list"; // Ruta de la plantilla Thymeleaf para usuarios
    }


    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * @param userDTO                Objeto que contiene los datos del formulario.
     * @param redirectAttributes  Atributos para mensajes flash.
     * @return Redirección a la lista de usuarios.
     */
    @PostMapping("/insert")
    public String insertUser(@Valid @ModelAttribute("user") UserCreateDTO userDTO,
                             BindingResult result,
                             Model model,
                             Locale locale,
                             RedirectAttributes redirectAttributes) {

        logger.info("Insertando nuevo usuario con email {}", userDTO.getEmail());

        try {
            if (result.hasErrors()) {
                model.addAttribute("allRoles", roleDAO.listAllRoles());
                return "views/user/user-form";
            }

            if (userDAO.existsUserByEmail(userDTO.getEmail())) {
                logger.warn("User con email {} existe", userDTO.getEmail());
                String errorMessage =  messageSource.getMessage("msg.user-controller.insert.emailExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return  "redirect:/users/new";
            }

            LocalDateTime lastPasswordChange = userDTO.getLastPasswordChange();
            if (lastPasswordChange == null) {
                lastPasswordChange = LocalDateTime.now();
                userDTO.setPasswordExpiresAt(lastPasswordChange);
            }

            LocalDateTime passwordExpiresAt = lastPasswordChange.plusDays(PASSWORD_EXPIRY_DAYS);
            userDTO.setPasswordExpiresAt(passwordExpiresAt);

            var roles = new HashSet<>(roleDAO.findByIds(userDTO.getRoleIds()));
            User user = UserMapper.toEntity(userDTO, roles);

            userDAO.insertUser(user);

            logger.info("Usuario {} insertado con éxito.", user.getEmail());

        } catch (Exception e) {
            logger.error("Error al insertar el usuario {}: {}", userDTO.getEmail(), e.getMessage(), e);
            model.addAttribute("errorMessage", messageSource.getMessage(
                    "msg.user-controller.insert.error", null, locale));
            return "views/user/user-form";
        }

        return "redirect:/users";
    }

    /**
     * Actualiza un usuario existente.
     *
     * @param userDTO             DTO con los datos actualizados del usuario.
     * @param result              Resultado de validación.
     * @param redirectAttributes  Atributos para mensajes flash.
     * @param locale              Idioma actual.
     * @return Redirección a la lista de usuarios.
     */
    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") UserUpdateDTO userDTO,
                             BindingResult result,
                             Model model,
                             Locale locale, RedirectAttributes redirectAttributes) {

        logger.info("Actualizando usuario con ID {}", userDTO.getId());

        try {
            if (result.hasErrors()) {
                model.addAttribute("allRoles", roleDAO.listAllRoles());
                return "views/user/user-form";
            }

            if (userDAO.existsUserByEmailAndNotId(userDTO.getEmail(), userDTO.getId())) {
                logger.warn("El correo {} ya existe para otro usuario.", userDTO.getEmail());
                String errorMessage = messageSource.getMessage("msg.user-controller.update.emailExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/edit?id=" + userDTO.getId();
            }

            LocalDateTime lastPasswordChange = userDTO.getLastPasswordChange();
            if (lastPasswordChange == null) {
                lastPasswordChange = LocalDateTime.now();
                userDTO.setLastPasswordChange(lastPasswordChange);
            }

            LocalDateTime passwordExpiresAt = lastPasswordChange.plusDays(PASSWORD_EXPIRY_DAYS);
            userDTO.setPasswordExpiresAt(passwordExpiresAt);

            var roles = new HashSet<>(roleDAO.findByIds(userDTO.getRoleIds()));
            User user = UserMapper.toEntity(userDTO, roles);

            userDAO.updateUser(user);
            logger.info("Usuario actualizado con ID {} actualizado. Expira el {}", userDTO.getId(), passwordExpiresAt);

        } catch (Exception e) {
            logger.error("Error al actualizar el usuario con ID {}: {}", userDTO.getId(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/users";
    }



    /**
     * Muestra el detalle de un usuario específico.
     *
     * @param id                  Identificador del usuario a consultar.
     * @param model               Modelo de Spring MVC para pasar datos a la vista.
     * @param redirectAttributes  Mensajes flash al redirigir (errores/avisos).
     * @param locale              Configuración regional para internacionalización de mensajes.
     * @return Plantilla Thymeleaf {@code views/user/user-detail} o redirección a {@code /users}.
     */
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Mostrando detalle del usuario con ID {}", id);
        try {
            User user = userDAO.getUserById(id);
            if (user == null) {
                logger.warn("No se encontró el usuario con ID {}", id);
                String msg = messageSource.getMessage("msg.user-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/users";
            }

            UserDetailDTO userDetailDTO = UserMapper.toDetailDTO(user);
            model.addAttribute("user", userDetailDTO);

            return "views/user/user-detail";
        } catch (Exception e) {
            logger.error("Error al obtener el detalle del usuario {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.user-controller.detail.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/users";
        }
    }


    /**
     * Elimina un usuario de la base de datos.
     *
     * @param id                 ID del usuario a eliminar.
     * @param redirectAttributes Atributos para mensajes flash.
     * @param locale             Idioma actual.
     * @return Redirección a la lista de usuarios.
     */
    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Eliminando usuario con ID: {}", id);
        try {
            // Eliminar usuario desde el DAO
            userDAO.deleteUser(id);
            logger.info("Usuario con ID {} eliminado con éxito.", id);
            String successMessage = messageSource.getMessage(
                    "msg.user-controller.delete.success", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (Exception e) {
            logger.error("Error al eliminar el usuario con ID {}: {}", id, e.getMessage(), e);
            String errorMessage = messageSource.getMessage(
                    "msg.user-controller.delete.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/users";
    }

}
