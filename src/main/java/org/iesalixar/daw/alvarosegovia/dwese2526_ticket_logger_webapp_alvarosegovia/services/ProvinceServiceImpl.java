package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.services;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.*;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Province;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Region;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.exeptions.DuplicateResourceException;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.exeptions.ResourceNotFoundException;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers.ProvinceMapper;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.mappers.RegionMapper;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories.ProvinceRepository;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de provincias.
 * <p>
 * Proporciona la lógica de negocio necesaria para la creación,
 * consulta, actualización y eliminación de provincias, así como
 * la obtención de distintos DTOs según el caso de uso.
 * </p>
 */
@Service
@Transactional
public class ProvinceServiceImpl implements ProvinceService {

    /** Repositorio de acceso a datos de provincias */
    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private RegionRepository regionRepository;

    /**
     * Obtiene una lista paginada de provincias.
     *
     * @param pageable información de paginación y ordenación
     * @return página de provincias en formato DTO
     */
    @Override
    public Page<ProvinceDTO> list(Pageable pageable) {
        return provinceRepository.findAll(pageable)
                .map(ProvinceMapper::toDTO);
    }

    /**
     * Obtiene los datos de una provincia para su edición.
     *
     * @param id identificador de la provincia
     * @return DTO con los datos editables de la provincia
     * @throws ResourceNotFoundException si la provincia no existe
     */
    @Override
    public ProvinceUpdateDTO getForEdit(Long id) {
        Province province = provinceRepository.findByIdWithRegion(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("province", "id", id)
                );

        return ProvinceMapper.toUpdateDTO(province);
    }


    /**
     * Crea una nueva provincia.
     * <p>
     * Antes de la creación se comprueba que no exista otra provincia
     * con el mismo nombre.
     * </p>
     *
     * @param dto DTO con los datos de creación
     * @throws DuplicateResourceException si el nombre ya existe
     */
    @Override
    public void create(ProvinceCreateDTO dto) {

        if (provinceRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException(
                    "province", "name", dto.getName());
        }

        Province province = ProvinceMapper.toEntity(dto);
        provinceRepository.save(province);
    }

    /**
     * Actualiza una provincia existente.
     * <p>
     * Se valida que el código de la provincia no esté duplicado
     * en otra entidad distinta.
     * </p>
     *
     * @param dto DTO con los datos actualizados
     * @throws DuplicateResourceException si el código ya existe
     * @throws ResourceNotFoundException si la provincia no existe
     */
    @Override
    public void update(ProvinceUpdateDTO dto) {

        if (provinceRepository.existsByCodeAndIdNot(
                dto.getCode(), dto.getId())) {
            throw new DuplicateResourceException(
                    "province", "code", dto.getCode());
        }

        Province province = provinceRepository.findById(dto.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "province", "id", dto.getId())
                );

        ProvinceMapper.copyToExistingEntity(dto, province);
        provinceRepository.save(province);
    }

    /**
     * Elimina una provincia por su identificador.
     *
     * @param id identificador de la provincia
     * @throws ResourceNotFoundException si la provincia no existe
     */
    @Override
    public void delete(Long id) {

        if (!provinceRepository.existsById(id)) {
            throw new ResourceNotFoundException("province", "id", id);
        }

        provinceRepository.deleteById(id);
    }

    /**
     * Obtiene el detalle completo de una provincia, incluyendo su región.
     *
     * @param id identificador de la provincia
     * @return DTO con la información detallada de la provincia
     * @throws ResourceNotFoundException si la provincia no existe
     */
    @Override
    public ProvinceDetailDTO getDetail(Long id) {

        Province province = provinceRepository.findByIdWithRegion(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("province", "id", id)
                );

        return ProvinceMapper.toDetailDTO(province);
    }
}
