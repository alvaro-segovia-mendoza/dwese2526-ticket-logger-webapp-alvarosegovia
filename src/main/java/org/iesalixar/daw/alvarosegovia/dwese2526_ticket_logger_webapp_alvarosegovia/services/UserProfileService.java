package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.services;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.UserProfileFormDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {

    UserProfileFormDTO getFormByEmail(String email);

    void updateProfile(String email, UserProfileFormDTO profileDTO, MultipartFile profileImageFile);
}
