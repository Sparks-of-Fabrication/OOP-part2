package com.sparks.of.fabrication.oop2.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityManagerWrapper {

    private static final Logger log = LogManager.getLogger(EntityManagerWrapper.class);
    EntityManagerFactory emf;
    EntityManager em;

    public EntityManagerWrapper(Env env) {
        try {
            Map<String, String> properties = getProperties(env);

            emf = Persistence.createEntityManagerFactory("jpaOOP", properties);

            if (emf == null) {
                log.error("ENTITY MANAGET UNINITIALIZED");
            }
            em = emf.createEntityManager();

            if (em != null) {
                log.info("Successfully created EntityManager and connected to the database.");
            }

        } catch (Exception e) {
            log.error("Error Initializing Entity manager factory CAUSE: {} MESSAGE: {}" , e.getMessage(), e.getCause());
        }
    }

    @NotNull
    private static Map<String, String> getProperties(Env env) {
        Map<String, String> properties = new HashMap<>();

        properties.put("jakarta.persistence.jdbc.url", env.getDbUrl());
        properties.put("jakarta.persistence.jdbc.user", env.getDbUser());
        properties.put("jakarta.persistence.jdbc.password", env.getDbPassword());
        properties.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
        properties.put("hibernate.hbm2ddl.auto", "update"); // Or "create", "create-drop"

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

    public <T, Y> Pair<Boolean, T> findEntityByVal(Class<T> tClass, Field field, Y value) {
        try{
            String jpql = "SELECT e FROM " + tClass.getSimpleName() + " e WHERE e." + field.getName() + " = :value";
            TypedQuery<T> query = this.em.createQuery(jpql, tClass);
            query.setParameter("value", value);

            T entity = query.getSingleResult();

            return new Pair<>(true, entity);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return new Pair<>(false, null);
        }
    }

    public <T, Y> Pair<Boolean, List<T>> findEntityByValAll(Class<T> tClass, Field field, Y value) {
        try{
            String jpql = "SELECT e FROM " + tClass.getSimpleName() + " e WHERE e." + field.getName() + " = :value";
            TypedQuery<T> query = this.em.createQuery(jpql, tClass);
            query.setParameter("value", value);

            List<T> entity = query.getResultList();

            return new Pair<>(true, entity);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return new Pair<>(false, null);
        }
    }

    public <T> List<T> findAllEntities(Class<T> tClass) {
        try {
            String jpql = "SELECT e FROM " + tClass.getSimpleName() + " e";
            TypedQuery<T> query = em.createQuery(jpql, tClass);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error fetching all entities for {}: {}", tClass.getSimpleName(), e.getMessage(), e);
            return new ArrayList<>();
        }
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

    public <T> Pair<Boolean, T> findEntityById(Class<T> tClass, int id) {
        try {
            T entity = em.find(tClass, id);

            log.info("Found entity: {}", entity);
            return new Pair<>(true, entity);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            rollbackTransaction();
            return new Pair<>(false, null);
        }
    }

    public <T> boolean deleteEntityById(Class<T> tClass, int id) {
        try {
            T entity = em.find(tClass, id);
            beginTransaction();
            em.remove(entity);
            commitTransaction();

            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            rollbackTransaction();

            return false;
        }
    }

    public <T> boolean updateEntityByFields(Class<T> tClass, int id, HashMap<Field, ?> data) {
        try {
            @NotNull
            StringBuilder jpql = new StringBuilder();

            jpql.append("UPDATE ");
            jpql.append(tClass.getSimpleName());
            jpql.append(" SET ");

            data.forEach((field, input) -> {
                jpql.append(field.getName()).append(" = ").append(input);
            });

            jpql.append(" WHERE ").append("id = ").append(id);

            em.createQuery(jpql.toString(), tClass).executeUpdate();

            return false;
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            rollbackTransaction();
            return false;
        }
    }

    public boolean cleanUp() {
        try {
            if(em != null && em.isOpen()) {
                em.close();
            }
            if(emf != null && emf.isOpen()) {
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












