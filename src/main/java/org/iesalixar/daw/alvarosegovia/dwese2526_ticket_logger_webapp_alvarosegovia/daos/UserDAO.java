package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;

import java.sql.SQLException;
import java.util.List;

public interface UserDAO {

    List<User> listAllUsers();
    List<User> listUsersPage(int page, int size, String sortField, String sortDir);
    long countUsers();
    void insertUser (User user);
    void updateUser (User user);
    void deleteUser(Long id);
    User getUserById(Long id);
    boolean existsUserByUsername (String username);
    boolean existsUserByUsernameAndNotId(String username, Long id);
}
