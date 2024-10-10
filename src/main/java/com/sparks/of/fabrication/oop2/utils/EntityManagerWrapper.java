package com.sparks.of.fabrication.oop2.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class EntityManagerWrapper {

    private static final Logger log = LogManager.getLogger(EntityManagerWrapper.class);
    EntityManagerFactory emf;
    EntityManager em;

    public EntityManagerWrapper(Env env) {
        try {
            Map<String, String> properties = getProperties(env);

            emf = Persistence.createEntityManagerFactory("jpaOOP", properties);

            if (emf != null) {
                log.info("Successfully initialized EntityManagerFactory.");
            }

            assert emf != null;
            em = emf.createEntityManager();

            if (em != null) {
                log.info("Successfully created EntityManager and connected to the database.");
            }

        } catch (Exception e) {
            log.error("Error Initializing Entity manager factory {}", e.getMessage());
        }
    }

    @NotNull
    private static Map<String, String> getProperties(Env env) {
        Map<String, String> properties = new HashMap<>();

        properties.put("javax.persistence.jdbc.url", env.getDbUrl());
        properties.put("javax.persistence.jdbc.user", env.getDbUser());
        properties.put("javax.persistence.jdbc.password", env.getDbPassword());
        properties.put("javax.persistence.jdbc.driver", "org.postgresql.Driver");
        properties.put("jakarta.persistence.provider", "org.hibernate.jpa.HibernatePersistenceProvider");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        return properties;
    }

    private void beginTransaction() {
        em.getTransaction().begin();
    }

    private void commitTransaction() {
        em.getTransaction().commit();
    }

    private void rollbackTransaction() {
        em.getTransaction().rollback();
    }

    public <T> boolean genEntity(T entity) {
        try {
            beginTransaction();
            em.persist(entity);
            commitTransaction();
            log.info("Persisted entity: {}", entity);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            rollbackTransaction();
            return false;
        }
    }

    public <T> Pair<Boolean, T> findEntityById(Class<T> tClass, String id) {
        try {
            beginTransaction();
            T entity = em.find(tClass, id);
            commitTransaction();
            log.info("Found entity: {}", entity);
            return new Pair<>(true, entity);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            rollbackTransaction();
            return new Pair<>(false, null);
        }
    }

    public boolean cleanUp() {
        try {
            if(em != null) {
                em.close();
            }
            if(emf != null) {
                emf.close();
            }
            log.info("Cleared EntityManager");
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }
}
