package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
@Transactional
public class RoleDAOImpl implements  RoleDAO
{

    private static final Logger logger = LoggerFactory.getLogger(RoleDAOImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Role> listAllRoles() {
        logger.info("Listing all roles from the database");
        String hql = "SELECT r FROM Role r ORDER BY r.name";
        List<Role> roles = em.createQuery(hql, Role.class).getResultList();
        logger.info("Retrieved {} from roles from the database", roles.size());
        return  roles;
    }

    @Override
    public List<Role> findByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            logger.info("findAllByIds called with null or empty ids. Returning empty list");
            return List.of();
        }

        logger.info("Finding roles by ids: {}", ids);
        String hql = "SELECT r FROM Role r WHERE r.id IN :ids";
        List<Role> roles = em.createQuery(hql, Role.class).setParameter("ids", ids).getResultList();
        logger.info("Found {} matching the given ids.", roles.size());
        return roles;
    }
}
