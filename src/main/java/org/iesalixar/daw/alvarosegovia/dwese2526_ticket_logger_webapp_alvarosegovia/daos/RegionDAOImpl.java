package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Implementación de {@link RegionDAO} usando {@link JdbcTemplate}.
 * Gestiona las operaciones CRUD sobre la tabla 'regions',
 */
@Repository // Anotación que marca esta clase como un componente que gestiona la persistencia.
public class RegionDAOImpl implements RegionDAO {

    // Logger para registrar eventos importantes en el DAO
    private static final Logger logger = LoggerFactory.getLogger(RegionDAOImpl.class);

    private final JdbcTemplate jdbcTemplate;

    // Inyección de JdbcTemplate
    public RegionDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Lista todas las regiones de la base de datos.
     * @return Lista de regiones
     */
    @Override
    public List<Region> listAllRegions() {
        logger.info("Listing all regions from the database.");
        String sql = "SELECT * FROM regions";
        List<Region> regions = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Region.class));
        logger.info("Retrieved {} regions from the database.", regions.size());
        return regions;
    }

    /**
     * Inserta una nueva región en la base de datos.
     * @param region Región a insertar
     */
    @Override
    public void insertRegion(Region region) {
        logger.info("Inserting region with code: {} and name {}", region.getCode(), region.getName());
        String sql = "INSERT INTO regions (code, name) VALUES (?, ?)";
        int rowsAffected = jdbcTemplate.update(sql, region.getCode(), region.getName());
        logger.info("Inserted region. Rows affected: {}", rowsAffected);
    }

    /**
     * Actualiza una región existente en la base de datos.
     * @param region Región a actualizar
     */
    @Override
    public void updateRegion(Region region) {
        logger.info("Updating region with id: {}", region.getId());
        String sql = "UPDATE regions SET code = ?, name = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, region.getCode(), region.getName(), region.getId());
        logger.info("Updated region. Rows affected: {}", rowsAffected);
    }

    /**
     * Elimina una región de la base de datos.
     * @param id ID de la región a eliminar
     */
    @Override
    public void deleteRegion(Long id) {
        logger.info("Deleting region with id: {}", id);
        String sql = "DELETE FROM regions WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        logger.info("Deleted region. Rows affected: {}", rowsAffected);
    }

    /**
     * Recupera una región por su ID.
     * @param id ID de la región a recuperar
     * @return Región encontrada o null si no existe
     */
    @Override
    public Region getRegionById(Long id) {
        logger.info("Retrieving region by id: {}", id);
        String sql = "SELECT * FROM regions WHERE id = ?";
        try {
            Region region = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Region.class), id);
            logger.info("Region retrieved: {} - {}", region.getCode(), region.getName());
            return region;
        } catch (Exception e) {
            logger.warn("No region found with id: {}", id);
            return null;
        }
    }

    /**
     * Verifica si una región con el código especificado ya existe en la base de datos.
     * @param code el código de la región a verificar.
     * @return true si es una región con el código ya existe, false de lo contrario.
     */
    @Override
    public boolean existsRegionByCode(String code) {
        logger.info("Checking if region with code: {} exists", code);
        String sql = "SELECT COUNT(*) FROM regions WHERE UPPER(code) = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code.toUpperCase());
        boolean exists = count != null && count > 0;
        logger.info("Region with code: {} exists: {}", code, exists);
        return exists;
    }

    /**
     * Verifica si una región con el código especificado ya existe en la base de datos,
     * excluyendo una región con ID específico.
     * @param code el código de la región a verificar.
     * @param id   el ID de la región a excluir de la verficación.
     * @return true si es una región con el código ya existe (y no es la región con el ID dado),
     *         false de lo contrario.
     */
    @Override
    public boolean existsRegionByCodeAndNotId(String code, Long id) {
        logger.info("Checking if region with code: {} exists excluding id: {}", code, id);
        String sql = "SELECT COUNT(*) FROM regions WHERE UPPER(code) = ? AND id != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code.toUpperCase(), id);
        boolean exists = count != null && count > 0;
        logger.info("Region with code: {} exists excluding id {}: {}", code, id, exists);
        return exists;
    }

    @Override
    public boolean existsRegionByName(String name) {
        logger.info("Checking if region with name: {} exists", name);
        String sql = "SELECT COUNT(*) FROM regions WHERE UPPER(name) = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name.toUpperCase());
        boolean exists = count != null && count > 0;
        logger.info("Region with name: {} exists: {}", name, exists);
        return exists;
    }
}
