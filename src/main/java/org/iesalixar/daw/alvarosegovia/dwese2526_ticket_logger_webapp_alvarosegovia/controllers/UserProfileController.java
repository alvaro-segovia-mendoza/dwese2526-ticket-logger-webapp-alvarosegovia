package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos.UserDAO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos.UserProfileDAO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.UserProfileFormDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.UserProfile;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers.UserProfileMapper;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.services.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

/**
 * Controlador para la gestión del perfil de usuario.
 * Permite ver y actualizar los datos del perfil, incluyendo la imagen de perfil.
 */
@Controller
@RequestMapping("/profile")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private MessageSource messageSource; // Para internacionalización de mensajes

    @Autowired
    private UserDAO userDAO; // DAO para acceder a usuarios

    @Autowired
    private UserProfileDAO userProfileDAO; // DAO para acceder a perfiles de usuario

    @Autowired
    private FileStorageService fileStorageService; // Servicio para gestionar archivos (imágenes)

    /**
     * Muestra el formulario de edición de perfil para un usuario fijo.
     *
     * @param model  Modelo de Spring MVC para pasar datos a la vista
     * @param locale Locale para mensajes internacionalizados
     * @return Nombre de la vista Thymeleaf
     */
    @GetMapping("/edit")
    public String showProfileForm(Model model, Locale locale) {
        final String fixedEmail = "admin@app.local"; // Usuario fijo para demo
        logger.info("Mostrando formulario de perfil para el usuario fijo {}", fixedEmail);

        // 1. Cargar la entidad User por email fijo
        User user = userDAO.getUserByEmail(fixedEmail);
        if (user == null) {
            logger.warn("No se encontró el usuario con email {}", fixedEmail);
            String errorMessage = messageSource.getMessage("msg.user-controller.edit.notFound", null, locale);
            model.addAttribute("errorMessage", errorMessage);
            return "views/user-profile/user-profile-form"; // Vista con mensaje de error
        }

        // 2. Cargar el perfil (puede no existir)
        UserProfile profile = userProfileDAO.getUserProfileByUserId(user.getId());

        // 3. Mapear User + UserProfile -> DTO de formulario
        UserProfileFormDTO formDTO = UserProfileMapper.toFormDto(user, profile);

        // 4. Enviar el modelo a la vista
        model.addAttribute("userProfileForm", formDTO);

        return "views/user-profile/user-profile-form";
    }

    /**
     * Actualiza los datos del perfil del usuario.
     * Incluye validación de formulario y gestión de imagen de perfil.
     *
     * @param profileDto           DTO con los datos del formulario
     * @param result               Resultado de validación
     * @param profileImageFile     Archivo subido para la imagen de perfil
     * @param redirectAttributes   Atributos para pasar mensajes tras redirección
     * @param locale               Locale para internacionalización
     * @return Redirección a la vista de edición
     */
    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("userProfileForm") UserProfileFormDTO profileDto,
                                BindingResult result,
                                @RequestParam("profileImageFile") MultipartFile profileImageFile,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {

        logger.info("Actualizando perfil para el usuario con id {}", profileDto.getUserId());

        // Validación de errores en el formulario
        if (result.hasErrors()) {
            logger.warn("Errores de validación en el formulario de perfil para userId={}", profileDto.getUserId());
            return "views/user-profile/user-profile-form";
        }

        try {
            Long userId = profileDto.getUserId();
            User user = userDAO.getUserById(userId);

            if (user == null) {
                logger.warn("No se encontró el usuario con id {}", userId);
                String errorMessage = messageSource.getMessage("msg.user-controller.edit.notfound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/profile/edit";
            }

            // Cargar perfil existente o preparar uno nuevo
            UserProfile profile = userProfileDAO.getUserProfileByUserId(userId);
            boolean isNew = (profile == null);

            if (isNew) {
                profile = UserProfileMapper.toNewEntity(profileDto, user);
            } else {
                UserProfileMapper.copyToExistingEntity(profileDto, profile);
            }

            // ──────────────── GESTIÓN DE LA IMAGEN DE PERFIL ────────────────
            if (profileImageFile != null && !profileImageFile.isEmpty()) {

                logger.info("Se ha subido un nuevo archivo de imagen para el perfil del usuario {}", userId);

                // Validación MIME (solo imágenes)
                String contentType = profileImageFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    logger.warn("Archivo de tipo no permitido: {}", contentType);
                    String msg = messageSource.getMessage("msg.userProfile.image.invalidType", null, locale);
                    redirectAttributes.addFlashAttribute("errorMessage", msg);
                    return "redirect:/profile/edit";
                }

                // Validación tamaño máximo 2MB
                long maxSizeBytes = 2 * 1024 * 1024;
                if (profileImageFile.getSize() > maxSizeBytes) {
                    logger.warn("Archivo demasiado grande");
                    String msg = messageSource.getMessage("msg.userProfile.image.tooLarge", null, locale);
                    redirectAttributes.addFlashAttribute("errorMessage", msg);
                    return "redirect:/profile/edit";
                }

                // Guardar archivo
                String oldImagePath = profileDto.getProfileImage();
                String newImageWebPath = fileStorageService.saveFile(profileImageFile);

                if (newImageWebPath == null) {
                    logger.error("No se pudo guardar la nueva imagen de perfil para el usuario {}", userId);
                    String msg = messageSource.getMessage("msg.userProfile.image.saveError", null, locale);
                    redirectAttributes.addFlashAttribute("errorMessage", msg);
                    return "redirect:/profile/edit";
                }

                logger.info("Nueva imagen de perfil guardada en {}", newImageWebPath);

                // Actualizar DTO para que el mapper copie esta ruta al entity
                profileDto.setProfileImage(newImageWebPath);

                // Eliminar imagen anterior si existía
                if (oldImagePath != null && !oldImagePath.isBlank()) {
                    logger.info("Eliminando imagen anterior de perfil: {}", oldImagePath);
                    fileStorageService.deleteFile(oldImagePath);
                }
            }

            // Copiar los datos finales del DTO al entity antes de guardar
            if (isNew) {
                profile = UserProfileMapper.toNewEntity(profileDto, user);
            } else {
                UserProfileMapper.copyToExistingEntity(profileDto, profile);
            }

            // ──────────────── GUARDAR EN BASE DE DATOS ────────────────
            userProfileDAO.saveOrUpdateUserProfile(profile);

            String successMessage = messageSource.getMessage("msg.userProfile.success", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);

        } catch (Exception e) {
            logger.error("Error al actualizar el perfil del usuario con id {}", profileDto.getUserId(), e);
            String errorMessage = messageSource.getMessage("msg.userProfile.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/profile/edit";
    }
}
