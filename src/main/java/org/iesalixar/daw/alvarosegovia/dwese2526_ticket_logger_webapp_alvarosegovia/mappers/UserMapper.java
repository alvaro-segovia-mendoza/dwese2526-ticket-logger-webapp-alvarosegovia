package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers;

import lombok.extern.slf4j.Slf4j;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.*;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.UserProfile;

import java.util.List;

/**
 * Mapper utilizado entre la entidad {@link User} y sus DTOs.
 * Implementación simple sin frameworks de mapeo.
 */
@Slf4j
public class UserMapper {

    // --------------------------------------
    // Entity -> DTO (básico)
    // --------------------------------------
    public static UserDTO toDTO(User entity) {
        if (entity == null) return null;
        return UserDTO.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .active(entity.getActive())
                .accountNonLocked(entity.getAccountNonLocked())
                .emailVerified(entity.getEmailVerified())
                .mustChangePassword(entity.getMustChangePassword())
                .lastPasswordChange(entity.getLastPasswordChange())
                .passwordExpiresAt(entity.getPasswordExpiresAt())
                .failedLoginAttempts(entity.getFailedLoginAttempts())
                .build();
    }

    public static List<UserDTO> toDTOList(List<User> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(UserMapper::toDTO).toList();
    }

    // --------------------------------------
    // Entity -> DTO (update)
    // --------------------------------------
    public static UserUpdateDTO toUpdateDTO(User entity) {
        if (entity == null) return null;
        return UserUpdateDTO.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .active(entity.getActive())
                .accountNonLocked(entity.getAccountNonLocked())
                .emailVerified(entity.getEmailVerified())
                .mustChangePassword(entity.getMustChangePassword())
                .lastPasswordChange(entity.getLastPasswordChange())
                .passwordExpiresAt(entity.getPasswordExpiresAt())
                .failedLoginAttempts(entity.getFailedLoginAttempts())
                .build();
    }

    // --------------------------------------
    // Entity -> DTO (detail)
    // --------------------------------------
    public static UserDetailDTO toDetailDTO(User entity) {
        if (entity == null) return null;

        UserDetailDTO dto = new UserDetailDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setPasswordHash(entity.getPasswordHash());
        dto.setActive(entity.getActive());
        dto.setAccountNonLocked(entity.getAccountNonLocked());
        dto.setLastPasswordChange(entity.getLastPasswordChange());
        dto.setPasswordExpiresAt(entity.getPasswordExpiresAt());
        dto.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        dto.setEmailVerified(entity.getEmailVerified());
        dto.setMustChangePassword(entity.getMustChangePassword());

        // Cargar datos del perfil si existe

        UserProfile profile = entity.getProfile();

        if (profile != null) {
            dto.setFirstName(profile.getFirstName());
            dto.setLastName(profile.getLastName());
            dto.setPhoneNumber(profile.getPhoneNumber());
            dto.setProfileImage(profile.getProfileImage());
            dto.setBio(profile.getBio());
            dto.setLocale(profile.getLocale());
        }

        return dto;
    }

    // --------------------------------------
    // DTO -> Entity
    // --------------------------------------
    public static User toEntity(UserCreateDTO dto) {
        if (dto == null) return null;
        User entity = new User();
        entity.setEmail(dto.getEmail());
        entity.setPasswordHash(dto.getPasswordHash());
        entity.setActive(dto.getActive());
        entity.setAccountNonLocked(dto.getAccountNonLocked());
        entity.setEmailVerified(dto.getEmailVerified());
        entity.setMustChangePassword(dto.getMustChangePassword());
        entity.setLastPasswordChange(dto.getLastPasswordChange());
        entity.setPasswordExpiresAt(dto.getPasswordExpiresAt());
        entity.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        return entity;
    }

    public static User toEntity(UserUpdateDTO dto) {
        if (dto == null) return null;
        User entity = new User();
        entity.setId(dto.getId()); // importante para update
        entity.setEmail(dto.getEmail());
        entity.setPasswordHash(dto.getPasswordHash());
        entity.setActive(dto.getActive());
        entity.setAccountNonLocked(dto.getAccountNonLocked());
        entity.setEmailVerified(dto.getEmailVerified());
        entity.setMustChangePassword(dto.getMustChangePassword());
        entity.setLastPasswordChange(dto.getLastPasswordChange());
        entity.setPasswordExpiresAt(dto.getPasswordExpiresAt());
        entity.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        return entity;
    }

    // --------------------------------------
    // Copiar DTO -> Entity existente
    // --------------------------------------
    public static void copyToExistingEntity(UserUpdateDTO dto, User entity) {
        if (dto == null || entity == null) return;
        entity.setEmail(dto.getEmail());
        entity.setPasswordHash(dto.getPasswordHash());
        entity.setActive(dto.getActive());
        entity.setAccountNonLocked(dto.getAccountNonLocked());
        entity.setEmailVerified(dto.getEmailVerified());
        entity.setMustChangePassword(dto.getMustChangePassword());
        entity.setLastPasswordChange(dto.getLastPasswordChange());
        entity.setPasswordExpiresAt(dto.getPasswordExpiresAt());
        entity.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        // No tocar entity.setId() ni relaciones
    }
}
