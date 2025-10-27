package com.ticketmaster.exp.repository;

import com.ticketmaster.exp.MatchType;
import com.ticketmaster.exp.model.ExperimentRun;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExperimentRunRepositoryIT {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private ExperimentRunRepository repository;

    @BeforeAll
    public static void setUpClass() {
        // Create the EntityManagerFactory once for all tests
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
        repository = new ExperimentRunRepository();

        // Clean the table before each test
        em.getTransaction().begin();
        em.createQuery("DELETE FROM ExperimentRun").executeUpdate();
        em.getTransaction().commit();
    }

    @AfterEach
    public void tearDown() {
        if (em != null) {
            em.close();
        }
    }

    @Test
    public void testSave() {
        // 1. Arrange: Create a test entity
        ExperimentRun runToSave = new ExperimentRun();
        runToSave.setExperimentKey("test-save-key");
        runToSave.setMatchType(MatchType.MATCH);
        runToSave.setStartTime(Instant.now());
        runToSave.setControlDurationMs(10L);
        runToSave.setCandidateDurationMs(12L);
        runToSave.setControlValue("{\"result\":\"OK\"}");
        runToSave.setCandidateValue("{\"result\":\"OK\"}");

        // 2. Act: Save it using the repository
        repository.save(runToSave);

        // 3. Assert: Verify it was saved correctly by fetching it back
        ExperimentRun foundRun = null;
        try {
            foundRun = em.createQuery("SELECT e FROM ExperimentRun e WHERE e.experimentKey = :key", ExperimentRun.class)
                    .setParameter("key", "test-save-key")
                    .getSingleResult();
        } catch (NoResultException e) {
            // This will cause the test to fail if nothing is found
        }

        assertNotNull(foundRun, "The saved experiment run should not be null.");
        assertEquals("test-save-key", foundRun.getExperimentKey());
        assertEquals(MatchType.MATCH, foundRun.getMatchType());
        assertEquals("{\"result\":\"OK\"}", foundRun.getControlValue());
        assertEquals(12L, foundRun.getCandidateDurationMs());
    }
}