package com.ticketmaster.exp.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class JpaRepository<T> {

    public void save(T entity) {
        // 1. Obtain an EntityManager for a single unit of work
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = null;

        try {
            // 2. Begin a transaction
            transaction = entityManager.getTransaction();
            transaction.begin();

            // 3. Use the EntityManager to persist the entity
            // This makes the entity object managed by the persistence context.
            entityManager.persist(entity);

            // 4. Commit the transaction to save changes to the database
            transaction.commit();
        } catch (Exception e) {
            // If any error occurs, roll back the transaction
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            // 5. Always close the EntityManager to release its resources
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }
}
