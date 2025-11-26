package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Province;

import java.util.List;

public interface ProvinceDAO {
    List<Province> listAllProvinces();
    List<Province> listProvincesPage(int page, int size, String sortField, String sortDir);
    long countProvinces();
    void insertProvince(Province province);
    void updateProvince(Province province);
    void deleteProvince(Long id);
    Province getProvinceById(Long id);
    boolean existsProvinceByCode(String code);
    boolean existsProvinceByCodeAndNotId(String code, Long id);
    boolean existsProvinceByName(String name);
}
