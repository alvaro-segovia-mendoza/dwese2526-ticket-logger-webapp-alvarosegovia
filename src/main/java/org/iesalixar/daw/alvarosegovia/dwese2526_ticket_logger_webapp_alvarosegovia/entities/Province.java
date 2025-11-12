package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Province {

    // Identificador único de la provincia (AUTO_INCREMENT en la tabla 'provinces').
    private Long id;

    // Código único de la provincia (VARCHAR(10) NOT NULL UNIQUE).
    @NotEmpty(message = "{msg.province.code.notEmpty}")
    @Size(max = 10, message = "{msg.province.code.size}")
    private String code;

    // Nombre de la provincia (VARCHAR(100) NOT NULL).
    @NotEmpty(message = "{msg.province.name.notEmpty}")
    @Size(max = 100, message = "{msg.province.name.size}")
    private String name;

    // Region asociada (puede venir instanciada pero con id NULL) por eso necesito una validación derivada
    private Region region;

    /**
     * Validación derivada:
     * Debe haberse seleccionado una región válida (region != null y region.id != null).
     * Esto evita el problema de tener un objeto Región vacío que cumple @NotNull.
     */
    @AssertTrue(message = "{msg.province.region.notNull}")
    public boolean isRegionSelected() {
        return region != null && region.getId() != null;
    }

    /**
     * Constructor de conveniencia sin {@code id}
     * útil para altas donde el ID se autogenera en base de datos.
     *
     * @param code
     * @param name
     * @param region
     */
    public Province(String code, String name, Region region) {
        this.code = code;
        this.name = name;
        this.region = region;
    }



}
