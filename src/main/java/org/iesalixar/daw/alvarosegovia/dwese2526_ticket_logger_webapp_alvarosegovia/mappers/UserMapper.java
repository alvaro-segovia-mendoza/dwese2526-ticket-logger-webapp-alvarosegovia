package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers;

import lombok.extern.slf4j.Slf4j;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.*;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Role;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.UserProfile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        UserDTO dto = new UserDTO();
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

        if (entity.getRoles() != null && !entity.getRoles().isEmpty()) {
            Set<String> roleName = entity.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
            dto.setRoles(roleName);
        } else {
            dto.setRoles(new HashSet<>());
        }
        return dto;
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

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setPasswordHash(entity.getPasswordHash());
        dto.setActive(entity.getActive());
        dto.setAccountNonLocked(entity.getAccountNonLocked());
        dto.setEmailVerified(entity.getEmailVerified());
        dto.setMustChangePassword(entity.getMustChangePassword());
        dto.setLastPasswordChange(entity.getLastPasswordChange());
        dto.setPasswordExpiresAt(entity.getPasswordExpiresAt());
        dto.setFailedLoginAttempts(entity.getFailedLoginAttempts());

        if (entity.getRoles() != null) {
            Set<Long> roleIds = entity.getRoles().stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet());
            dto.setRoleIds(roleIds);
        }
        return dto;
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

        if (entity.getRoles() != null &&  !entity.getRoles().isEmpty()) {
            Set<String> roleNames = entity.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
            dto.setRoles(roleNames);
        } else {
            dto.setRoles(new HashSet<>());
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

    public static User toEntity(UserCreateDTO dto, Set<Role> roles) {
        if (dto == null) return null;

        User e = toEntity(dto);
        e.setRoles(roles);
        return e;
    }

    public static User toEntity(UserUpdateDTO dto, Set<Role> roles) {
        if (dto == null) return null;

        User e = toEntity(dto);
        e.setRoles(roles);
        return e;
    }
}
