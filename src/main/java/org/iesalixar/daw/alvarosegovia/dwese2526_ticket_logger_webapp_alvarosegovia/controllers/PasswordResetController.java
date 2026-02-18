package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.PasswordResetDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.PasswordResetRequestDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.services.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

/**
 * Controlador para el flujo de “Olvidé mi contraseña” y restablecimiento mediante token.
 * <p>
 * Buenas prácticas:
 * <ul>
 *   <li>No revela si el email existe (anti-enumeración).</li>
 *   <li>Token de un solo uso con caducidad.</li>
 *   <li>Mensajes internacionalizados mediante {@link MessageSource}.</li>
 * </ul>
 * </p>
 */
@Controller
@RequestMapping("/auth")
public class PasswordResetController {


    @Autowired
    private PasswordResetService passwordResetService;


    @Autowired
    private MessageSource messageSource;


    /**
     * Muestra el formulario donde el usuario introduce su email para solicitar el reset.
     *
     * @param model modelo para la vista.
     * @return plantilla del formulario “forgot password”.
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("dto", new PasswordResetRequestDTO());
        return "views/reset-password/forgot-password";
    }


    /**
     * Procesa la solicitud de recuperación y envía el email con el enlace si el usuario existe.
     * <p>
     * La respuesta es siempre la misma para evitar ataques de enumeración de usuarios.
     * </p>
     *
     * @param dto                datos del formulario (email).
     * @param result             resultado de validación.
     * @param request            request HTTP para obtener IP y User-Agent (auditoría).
     * @param redirectAttributes mensajes flash.
     * @return redirección al formulario con mensaje de éxito genérico.
     */
    @PostMapping("/forgot")
    public String handleForgotPassword(
            @Valid @ModelAttribute("dto") PasswordResetRequestDTO dto,
            BindingResult result,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "views/reset-password/forgot-password";
        }


        // Auditoría básica (IP y UA)
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");


        // No revela si existe el email (la lógica interna decide si envía o no)
        passwordResetService.requestPasswordReset(dto.getEmail(), ip, userAgent);


        Locale locale = LocaleContextHolder.getLocale();
        String msg = messageSource.getMessage("msg.password-reset.request.sent", null, locale);
        redirectAttributes.addFlashAttribute("successMessage", msg);


        return "redirect:/auth/forgot-password";
    }


    /**
     * Muestra el formulario para establecer la nueva contraseña a partir del token.
     *
     * @param token token recibido por query param.
     * @param model modelo para la vista.
     * @return plantilla del formulario de reset.
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        PasswordResetDTO dto = new PasswordResetDTO();
        dto.setToken(token);
        model.addAttribute("dto", dto);
        return "views/reset-password/reset-password";
    }


    /**
     * Valida el token y actualiza la contraseña del usuario.
     *
     * @param dto                token + nueva contraseña + confirmación.
     * @param result             resultado de validación.
     * @param redirectAttributes mensajes flash.
     * @return redirección a login si OK; si falla, vuelve a forgot con error genérico.
     */
    @PostMapping("/reset-password")
    public String handleResetPassword(
            @Valid @ModelAttribute("dto") PasswordResetDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        // Validación cruzada (confirmación)
        if (dto.getNewPassword() != null && dto.getConfirmPassword() != null
                && !dto.getNewPassword().equals(dto.getConfirmPassword())) {
            // Clave i18n recomendada; evita hardcodear texto aquí
            result.rejectValue("confirmPassword", "password.mismatch");
        }


        if (result.hasErrors()) {
            return "views/reset-password/reset-password";
        }


        Locale locale = LocaleContextHolder.getLocale();


        try {
            passwordResetService.resetPassword(dto.getToken(), dto.getNewPassword());


            String msg = messageSource.getMessage("msg.password-reset.success", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", msg);
            return "redirect:/login";


        } catch (IllegalArgumentException ex) {
            // Mensaje genérico: no distinguir “no existe”, “caducado”, “ya usado”…
            String msg = messageSource.getMessage("msg.password-reset.invalid", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/auth/forgot-password";
        }
    }
}
