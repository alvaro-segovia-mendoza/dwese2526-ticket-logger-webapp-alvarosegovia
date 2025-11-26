package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de detalle para Región. Incluye la lista de provincias asociadas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionDetailDTO {
    private Long id;
    private String code;
    private String name;
    private List<ProvinceDTO> provinces;
}
