package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.services;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.UserCreateDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.UserDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.UserDetailDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.UserUpdateDTO;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Role;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.exeptions.DuplicateResourceException;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.exeptions.ResourceNotFoundException;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers.UserMapper;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories.RoleRepository;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Page<UserDTO> list(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserMapper::toDTO);
    }

    @Override
    public UserUpdateDTO getForEdit(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("user", "id", id)
                );

        return UserMapper.toUpdateDTO(user);
    }

    @Override
    public void create(UserCreateDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException(
                    "user", "email", dto.getEmail()
            );
        }

        User user = UserMapper.toEntity(dto);
        userRepository.save(user);
    }

    @Override
    public void update(UserUpdateDTO dto) {

        if (userRepository.existsByEmailAndIdNot(dto.getEmail(), dto.getId())) {
            throw new DuplicateResourceException(
                    "user", "email", dto.getEmail()
            );
        }

        User user = userRepository.findById(dto.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "user", "id", dto.getId()
                        )
                );

        UserMapper.copyToExistingEntity(dto, user);

        if (dto.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(
                    roleRepository.findByIdIn(dto.getRoleIds())
            );
            user.setRoles(roles);
        }

        userRepository.save(user);
    }


    @Override
    public void delete(Long id) {

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("user", "id", id);
        }

        userRepository.deleteById(id);
    }

    @Override
    public UserDetailDTO getDetail(Long id) {

        User user = userRepository.findByIdWithProfile(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("user", "id", id)
                );

        return UserMapper.toDetailDTO(user);
    }

    @Override
    public List<Role> listRoles() {
        return roleRepository.findAll();
    }

}
