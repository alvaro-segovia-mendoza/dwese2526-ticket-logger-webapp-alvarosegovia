package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Region;

import java.util.List;

public interface RegionDAO {

    List<Region> listAllRegions();
    void insertRegion (Region region);
    void updateRegion (Region region);
    void deleteRegion(Long id);
    Region getRegionById(Long id);
    boolean existsRegionByCode (String code);
    boolean existsRegionByCodeAndNotId(String code, Long id);
}
