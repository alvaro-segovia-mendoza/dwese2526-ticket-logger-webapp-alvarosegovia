package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos.UserDAO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.RegionUpdateDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.UserCreateDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.UserDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.UserUpdateDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Region;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers.RegionMapper;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    // DAO para gestionar las operaciones de las regiones en la base de datos
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PasswordEncoder passwordEncoder;


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
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Mostrando formulario de edición para la región con ID {}", id);
        User user = null;
        UserUpdateDTO userDTO = null;
        try {
            user = userDAO.getUserById(id);
            if (userDTO == null) {
                logger.warn("No se encontró el usuario con ID {}", id);
            }
            userDTO = UserMapper.toUpdateDTO(user);
        } catch (Exception e) {
            logger.error("Error al obtener el usuario con ID {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Error al obtener el usuario.");
        }
        model.addAttribute("user", userDTO);
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
            @RequestParam(name = "sortField", defaultValue = "username") String sortField,
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
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Insertando nuevo usuario con nombre {}", userDTO.getUsername());

        try {
            // Si hay errores de validación, volver al formulario
            if (result.hasErrors()) {
                return "views/user/user-form";
            }

            // Comprobar si el username ya existe
            if (userDAO.existsUserByUsername(userDTO.getUsername())) {
                logger.warn("El nombre de usuario {} ya existe.", userDTO.getUsername());
                String errorMessage = messageSource.getMessage(
                        "msg.user-controller.insert.usernameExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/new";
            }

            // Mapear DTO -> Entity
            User user = UserMapper.toEntity(userDTO);

            // Hashear la contraseña
            user.setPasswordHash(passwordEncoder.encode(userDTO.getPasswordHash()));

            // Insertar en BD
            userDAO.insertUser(user);

            logger.info("Usuario {} insertado con éxito.", user.getUsername());

        } catch (Exception e) {
            logger.error("Error al insertar el usuario {}: {}", userDTO.getUsername(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage(
                    "msg.user-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/users"; // Redirigir a la lista de usuarios
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
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Actualizando usuario con ID {}", userDTO.getId());
        try {
            if (result.hasErrors()) {
                return "views/user/user-form";
            }

            // Comprobar si el username ya existe para otro usuario
            if (userDAO.existsUserByUsernameAndNotId(userDTO.getUsername(), userDTO.getId())) {
                logger.warn("El nombre de usuario {} ya existe para otro usuario.", userDTO.getUsername());
                String errorMessage = messageSource.getMessage(
                        "msg.user-controller.update.usernameExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/edit?id=" + userDTO.getId();
            }

            // Recuperar la entidad existente de la BD
            User existingUser = userDAO.getUserById(userDTO.getId());
            if (existingUser == null) {
                logger.warn("Usuario con ID {} no encontrado.", userDTO.getId());
                String errorMessage = messageSource.getMessage(
                        "msg.user-controller.update.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users";
            }

            // Copiar campos del DTO a la entidad
            UserMapper.copyToExistingEntity(userDTO, existingUser);

            // Hashear la contraseña solo si se ha proporcionado una nueva
            if (userDTO.getPasswordHash() != null && !userDTO.getPasswordHash().isBlank()) {
                existingUser.setPasswordHash(passwordEncoder.encode(userDTO.getPasswordHash()));
            }

            // Actualizar en la base de datos
            userDAO.updateUser(existingUser);

            logger.info("Usuario con ID {} actualizado con éxito.", userDTO.getId());

        } catch (Exception e) {
            logger.error("Error al actualizar el usuario con ID {}: {}", userDTO.getId(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage(
                    "msg.user-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/users";
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
