package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO genérico de lectura para Province.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceDTO {
    private Long id;
    private String code;
    private String name;
    private String regionName;

}
