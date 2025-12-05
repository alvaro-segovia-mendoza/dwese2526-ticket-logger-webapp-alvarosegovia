package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Role;

import java.util.List;
import java.util.Set;

public interface RoleDAO {
    List<Role> listAllRoles();
    List<Role> findByIds(Set<Long> ids);
}
