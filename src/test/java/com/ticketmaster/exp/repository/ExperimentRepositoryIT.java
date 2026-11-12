package com.ticketmaster.exp.repository;

import com.ticketmaster.exp.model.Experiment;
import com.ticketmaster.exp.model.ReturnChoice;
import com.ticketmaster.exp.model.ReturnChoiceStrategy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class ExperimentRepositoryIT {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private ExperimentRepository repository;

    @BeforeAll
    public static void setUpClass() {
        // Create the EntityManagerFactory once for all tests using the persistence unit
        emf = Persistence.createEntityManagerFactory("experiment-pu");
    }

    @AfterAll
    public static void tearDownClass() {
        if (emf != null) {
            emf.close();
        }
    }

    @BeforeEach
    public void setUp() {
        // Create a new EntityManager and repository for each test
        em = emf.createEntityManager();
        repository = new ExperimentRepository();

        // Clean the table before each test to ensure isolation
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Experiment").executeUpdate();
        em.getTransaction().commit();
    }

    @AfterEach
    public void tearDown() {
        if (em != null) {
            em.close();
        }
    }

    @Test
    public void testSaveAndFind() {
        // 1. Arrange: Create a test entity with an embedded object
        ReturnChoice choice = new ReturnChoice();
        choice.setName(ReturnChoiceStrategy.FIND_FASTEST);
        choice.setMetadata("some-metadata-for-fastest");

        Experiment experimentToSave = new Experiment();
        experimentToSave.setName("user-profile-service-experiment");
        experimentToSave.setControl("com.example.UserServiceV1");
        experimentToSave.setCandidate("com.example.UserServiceV2");
        experimentToSave.setSameWhen("com.example.UserPredicate");
        experimentToSave.setExceptionWhen("com.example.ExceptionComparator");
        experimentToSave.setPublisher("com.example.LoggingPublisher");
        experimentToSave.setReturnChoice(choice);

        // 2. Act: Save it using the repository
        repository.save(experimentToSave);

        // 3. Assert: Verify it was saved correctly by fetching it back
        Experiment foundExperiment = null;
        try {
            foundExperiment = em.createQuery("SELECT e FROM Experiment e WHERE e.name = :name", Experiment.class)
                    .setParameter("name", "user-profile-service-experiment")
                    .getSingleResult();
        } catch (NoResultException e) {
            fail("No experiment was found with the given name, but it should have been saved.");
        }

        assertNotNull(foundExperiment, "The saved experiment should not be null.");
        assertNotNull(foundExperiment.getId(), "The ID should be generated and not null.");
        assertEquals("user-profile-service-experiment", foundExperiment.getName());
        assertEquals("com.example.UserServiceV2", foundExperiment.getCandidate());
        assertNotNull(foundExperiment.getReturnChoice(), "The embedded ReturnChoice object should not be null.");
        assertEquals(ReturnChoiceStrategy.FIND_FASTEST, foundExperiment.getReturnChoice().getName());
        assertEquals("some-metadata-for-fastest", foundExperiment.getReturnChoice().getMetadata());
    }
}