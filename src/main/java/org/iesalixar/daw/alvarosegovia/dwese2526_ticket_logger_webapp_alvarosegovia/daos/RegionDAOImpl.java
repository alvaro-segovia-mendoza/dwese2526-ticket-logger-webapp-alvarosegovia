package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
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
 * Implementación de {@link RegionDAO}.
 * Gestiona las operaciones CRUD sobre la tabla 'regions',
 */
@Repository // Anotación que marca esta clase como un componente que gestiona la persistencia.
@Transactional
// Indica que todos los métodos de esta clase deben ejecutarse dentro de una transacción.
// Si algún método lanza una excepción en tiempo de ejecución, la transacción se revierte automáticamente (rollback).
// Esto asegura la integridad de los datos: todas las operaciones dentro del método se confirman o se cancelan juntas.
public class RegionDAOImpl implements RegionDAO {

    // Logger para registrar eventos importantes en el DAO
    private static final Logger logger = LoggerFactory.getLogger(RegionDAOImpl.class);

    @PersistenceContext
    private EntityManager entityManager;
    /**
     * Lista todas las regiones de la base de datos.
     * @return Lista de regiones
     */
    @Override
    public List<Region> listAllRegions() {
        logger.info("Listing all regions from the database.");
        String hql = "SELECT r FROM Region r";
        List<Region> regions = entityManager.createQuery(hql, Region.class).getResultList();
        logger.info("Retrieved {} regions from the database.", regions.size());
        return regions;
    }

    /**
     * Lista una página de regiones ordenadas por nombre
     *
     * @param page página actual (0-based)
     * @param size número de elementos por página
     * @param sortField campo por el que se ordenan los resultados (id, code, name).
     * @param sortDir   dirección de ordenación ("asc" o "desc").
     * @return lista de regiones para esta página
     */
    @Override
    public List<Region> listRegionsPage(int page, int size, String sortField, String sortDir) {
        logger.info("Listing regions page={}, size={} from the database.", page, size,
                sortField, sortDir);

        int offset = page * size;

        // 1. Creación de Criteria
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Region> cq = cb.createQuery(Region.class);
        Root<Region> root = cq.from(Region.class);
        // 2. Determina el campo de ordenación permitido (whitelist)
        Path<?> sortPath;

        switch (sortField) {
            case "id" -> sortPath = root.get("id");
            case "code" -> sortPath = root.get("code");
            case "name" -> sortPath = root.get("name");
            default -> {
                logger.warn("Unknown sortField '{}', defaulting to 'name'.", sortField);
                sortPath = root.get("name");
            }
        }

        // 3. Dirección de ordenación
        boolean descending = "desc".equalsIgnoreCase(sortDir);
        // cb.desc y cb.asc son funciones predefinidas de criteria para las ordenaciones
        Order order = descending ? cb.desc(sortPath) : cb.asc(sortPath);
        // 4. Aplicar ordenación a la query
        cq.select(root).orderBy(order);
        // 5. Crear TypedQuery, aplicar paginación y ejecutar
        return entityManager.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * Devuelve el número total de regiones.
     * @return
     */
    @Override
    public long countRegions() {
        String hql = "SELECT COUNT(r) FROM Region r";
        Long total = entityManager.createQuery(hql, Long.class).getSingleResult();
        return (total != null) ? total : 0L;
    }

    /**
     * Inserta una nueva región en la base de datos.
     * @param region Región a insertar
     */
    @Override
    public void insertRegion(Region region) {
        logger.info("Inserting region with code: {} and name {}", region.getCode(), region.getName());
        entityManager.persist(region);
        logger.info("Inserted region successfully");
    }

    /**
     * Actualiza una región existente en la base de datos.
     * @param region Región a actualizar
     */
    @Override
    public void updateRegion(Region region) {
        logger.info("Updating region with id: {}", region.getId());
        entityManager.merge(region);
        logger.info("Updated region with id: {}", region.getId());
    }

    /**
     * Elimina una región de la base de datos.
     * @param id ID de la región a eliminar
     */
    @Override
    public void deleteRegion(Long id) {
        logger.info("Deleting region with id: {}", id);
        Region region = entityManager.find(Region.class, id);
        if (region != null) {
            entityManager.remove(region);
            logger.info("Deleted region with id: {}", id);
        } else {
            logger.warn("Region with id: {} not found.", id);
        }
    }

    /**
     * Recupera una región por su ID.
     * @param id ID de la región a recuperar
     * @return Región encontrada o null si no existe
     */
    @Override
    public Region getRegionById(Long id) {
        logger.info("Retrieving region by id: {}", id);
        Region region = entityManager.find(Region.class, id);
        if (region != null) {
            logger.info("Region retrieved: {} - {}", region.getCode(), region.getName());
        } else {
            logger.warn("No region found with id: {}", id);
        }
        return region;
    }

    /**
     * Verifica si una región con el código especificado ya existe en la base de datos.
     * @param code el código de la región a verificar.
     * @return true si es una región con el código ya existe, false de lo contrario.
     */
    @Override
    public boolean existsRegionByCode(String code) {
        logger.info("Checking if region with code: {} exists", code);
        String hql = "SELECT COUNT(r) FROM Region r WHERE UPPER(r.code) = :code";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("code", code.toUpperCase())
                .getSingleResult();
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
        String hql = "SELECT COUNT(r) FROM Region r WHERE UPPER(r.code) = :code AND r.id != :id";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("code", code.toUpperCase())
                .setParameter("id", id)
                .getSingleResult();
        boolean exists = count != null && count > 0;
        logger.info("Region with code: {} exists excluding id {}: {}", code, id, exists);
        return exists;
    }

    @Override
    public boolean existsRegionByName(String name) {
        logger.info("Checking if region with name: {} exists", name);
        String hql = "SELECT COUNT(r) FROM Region r WHERE UPPER(r.name) = :name";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("name", name.toUpperCase())
                .getSingleResult();
        boolean exists = count != null && count > 0;
        logger.info("Region with name: {} exists: {}", name, exists);
        return exists;
    }
}
