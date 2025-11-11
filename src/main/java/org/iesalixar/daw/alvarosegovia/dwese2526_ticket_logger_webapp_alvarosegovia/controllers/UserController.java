package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos.UserDAO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;
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

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Lista todos los usuarios y los pasa al modelo para su visualización.
     *
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla Thymeleaf para listar los usuarios.
     */
    @GetMapping
    public String listUsers(Model model) {
        logger.info("Solicitando la lista de todos los usuarios...");
        List<User> listUsers = null;
        try {
            listUsers = userDAO.listAllUsers();
            logger.info("Se han cargado {} usuarios.", listUsers.size());
        } catch (Exception e) {
            logger.error("Error al listar los usuarios: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al listar los usuarios.");
        }
        model.addAttribute("listUsers", listUsers);
        return "views/user/user-list";
    }

    /**
     * Muestra el formulario para crear un nuevo usuario.
     *
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla Thymeleaf para el formulario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo usuario.");
        model.addAttribute("user", new User());
        return "views/user/user-form";
    }

    /**
     * Muestra el formulario para editar un usuario existente.
     *
     * @param id    ID del usuario a editar.
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla Thymeleaf para el formulario.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model, RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Mostrando formulario de edición para el usuario con ID {}", id);
        User user = null;
        try {
            user = userDAO.getUserById(id);
            if (user == null) {
                logger.warn("No se encontró el usuario con ID {}", id);
                String errorMessage = messageSource.getMessage("msg.user-controller.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users";
            }
            model.addAttribute("user", user);
        } catch (Exception e) {
            logger.error("Error al obtener el usuario con ID {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Error al obtener el usuario.");
        }
        return "views/user/user-form";
    }

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * @param user                Datos del usuario.
     * @param result              Resultado de validación.
     * @param redirectAttributes  Atributos para mensajes flash.
     * @param locale              Idioma actual.
     * @return Redirección a la lista de usuarios.
     */
    @PostMapping("/insert")
    public String insertUser(@Valid @ModelAttribute("user") User user,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Insertando nuevo usuario con nombre {}", user.getUsername());
        try {

            if (result.hasErrors()) {
                return "views/user/user-form";
            }
            if (userDAO.existsUserByUsername(user.getUsername())) {
                logger.warn("El nombre de usuario {} ya existe.", user.getUsername());
                String errorMessage = messageSource.getMessage("msg.user-controller.insert.usernameExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/new";
            }
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            userDAO.insertUser(user);
            logger.info("Usuario {} insertado con éxito.", user.getUsername());
        } catch (Exception e) {
            logger.error("Error al insertar el usuario {}: {}", user.getUsername(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/users";
    }

    /**
     * Actualiza un usuario existente.
     *
     * @param user                Datos actualizados del usuario.
     * @param result              Resultado de validación.
     * @param redirectAttributes  Atributos para mensajes flash.
     * @param locale              Idioma actual.
     * @return Redirección a la lista de usuarios.
     */
    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") User user,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Actualizando usuario con ID {}", user.getId());
        try {
            if (result.hasErrors()) {
                return "views/user/user-form";
            }
            if (userDAO.existsUserByUsernameAndNotId(user.getUsername(), user.getId())) {
                logger.warn("El nombre de usuario {} ya existe para otro usuario.", user.getUsername());
                String errorMessage = messageSource.getMessage("msg.user-controller.update.usernameExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/edit?id=" + user.getId();
            }
            // Hashear la contraseña solo si se ha proporcionado una nueva
            if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            }
            userDAO.updateUser(user);
            logger.info("Usuario con ID {} actualizado con éxito.", user.getId());
        } catch (Exception e) {
            logger.error("Error al actualizar el usuario con ID {}: {}", user.getId(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/users";
    }

    /**
     * Elimina un usuario de la base de datos.
     *
     * @param id                 ID del usuario a eliminar.
     * @param redirectAttributes Atributos para mensajes flash.
     * @return Redirección a la lista de usuarios.
     */
    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id, RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Eliminando usuario con id: {}", id);
        try {
            userDAO.deleteUser(id);
            logger.info("Usuario con ID {} eliminado con éxito.", id);
        } catch (Exception e) {
            logger.error("Error al eliminar el usuario con ID {}: {}", id, e.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.delete.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/users";
    }
}
