package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.UserProfileFormDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.UserProfile;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers.UserProfileMapper;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories.UserProfileRepository;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories.UserRepository;
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
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);
    private static final String FIXED_EMAIL = "admin@app.local";

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/edit")
    public String showProfileForm(Model model, Locale locale) {
        logger.info("Mostrando formulario de perfil para el usuario fijo {}", FIXED_EMAIL);

        Optional<User> optionalUser = userRepository.findByEmail(FIXED_EMAIL);
        if (optionalUser.isEmpty()) {
            logger.warn("No se encontró el usuario con email {}", FIXED_EMAIL);
            String errorMessage = messageSource.getMessage("msg.user-controller.edit.notFound", null, locale);
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("userProfileForm", new UserProfileFormDTO());
            return "views/user-profile/user-profile-form";
        }

        User user = optionalUser.get();
        Optional<UserProfile> optionalProfile = userProfileRepository.findByUserId(user.getId());

        UserProfileFormDTO formDTO = UserProfileMapper.toFormDto(user, optionalProfile.orElse(null));
        model.addAttribute("userProfileForm", formDTO);

        return "views/user-profile/user-profile-form";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("userProfileForm") UserProfileFormDTO profileDto,
                                BindingResult result,
                                @RequestParam("profileImageFile") MultipartFile profileImageFile,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {

        if (result.hasErrors()) {
            logger.warn("Errores de validación en el formulario de perfil para userId={}", profileDto.getUserId());
            return "views/user-profile/user-profile-form";
        }

        try {
            Optional<User> optionalUser = userRepository.findById(profileDto.getUserId());
            if (optionalUser.isEmpty()) {
                logger.warn("No se encontró el usuario con id {}", profileDto.getUserId());
                String errorMessage = messageSource.getMessage("msg.user-controller.edit.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/profile/edit";
            }

            User user = optionalUser.get();
            UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElseGet(() ->
                    UserProfileMapper.toNewEntity(profileDto, user)
            );

            handleProfileImage(profileDto, profileImageFile, redirectAttributes, locale);

            UserProfileMapper.copyToExistingEntity(profileDto, profile);
            userProfileRepository.save(profile);

            String successMessage = messageSource.getMessage("msg.userProfile.success", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);

        } catch (Exception e) {
            logger.error("Error al actualizar el perfil del usuario con id {}", profileDto.getUserId(), e);
            String errorMessage = messageSource.getMessage("msg.userProfile.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/profile/edit";
    }

    // ────────────── MÉTODOS PRIVADOS AUXILIARES ──────────────
    private void handleProfileImage(UserProfileFormDTO profileDto, MultipartFile file,
                                    RedirectAttributes redirectAttributes, Locale locale) {
        if (file == null || file.isEmpty()) return;

        logger.info("Se ha subido un nuevo archivo de imagen para el perfil del usuario {}", profileDto.getUserId());

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            logger.warn("Archivo de tipo no permitido: {}", contentType);
            String msg = messageSource.getMessage("msg.userProfile.image.invalidType", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            throw new RuntimeException("Tipo de archivo no válido");
        }

        long maxSizeBytes = 2 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            logger.warn("Archivo demasiado grande");
            String msg = messageSource.getMessage("msg.userProfile.image.tooLarge", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            throw new RuntimeException("Archivo demasiado grande");
        }

        String oldImagePath = profileDto.getProfileImage();
        String newImageWebPath = fileStorageService.saveFile(file);

        if (newImageWebPath == null) {
            logger.error("No se pudo guardar la nueva imagen de perfil para el usuario {}", profileDto.getUserId());
            String msg = messageSource.getMessage("msg.userProfile.image.saveError", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            throw new RuntimeException("Error guardando imagen");
        }

        logger.info("Nueva imagen de perfil guardada en {}", newImageWebPath);
        profileDto.setProfileImage(newImageWebPath);

        if (oldImagePath != null && !oldImagePath.isBlank()) {
            logger.info("Eliminando imagen anterior de perfil: {}", oldImagePath);
            fileStorageService.deleteFile(oldImagePath);
        }
    }
}
