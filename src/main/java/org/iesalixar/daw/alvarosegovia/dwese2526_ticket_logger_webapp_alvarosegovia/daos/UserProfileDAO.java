package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.UserProfile;

public interface UserProfileDAO {

    UserProfile getUserProfileByUserId(Long userId);
    void saveOrUpdateUserProfile(UserProfile userProfile);
    boolean existsUserProfileByUserId(Long userId);

}
