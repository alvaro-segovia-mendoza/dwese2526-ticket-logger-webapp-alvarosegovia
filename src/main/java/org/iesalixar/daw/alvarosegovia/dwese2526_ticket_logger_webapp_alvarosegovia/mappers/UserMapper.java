package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.*;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;

import java.util.List;

/**
 * Mapper utilizado entre la entidad {@link User} y sus DTOs.
 * Implementación simple sin frameworks de mapeo.
 */
public class UserMapper {

    // --------------------------------------
    // Entity -> DTO (básico)
    // --------------------------------------
    public static UserDTO toDTO(User entity) {
        if (entity == null) return null;
        return UserDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
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
                .username(entity.getUsername())
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
        return new UserDetailDTO(
                entity.getId(),
                entity.getUsername(),
                entity.getPasswordHash(),
                entity.getActive(),
                entity.getAccountNonLocked(),
                entity.getEmailVerified(),
                entity.getMustChangePassword(),
                entity.getLastPasswordChange(),
                entity.getPasswordExpiresAt(),
                entity.getFailedLoginAttempts()
        );
    }

    // --------------------------------------
    // DTO -> Entity
    // --------------------------------------
    public static User toEntity(UserCreateDTO dto) {
        if (dto == null) return null;
        User entity = new User();
        entity.setUsername(dto.getUsername());
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
        entity.setUsername(dto.getUsername());
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
        entity.setUsername(dto.getUsername());
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
