package com.ticketmaster.exp.repository;

import com.ticketmaster.exp.model.Experiment;

/**
 * Repository for handling persistence of Experiment entities.
 * It extends the generic JpaRepository to inherit common save/find operations.
 */
public class ExperimentRepository extends JpaRepository<Experiment> {
    // This class can be extended with Experiment-specific query methods if needed.
    // For example:
    //
    // public Experiment findByName(String name) {
    //     // implementation to query by name
    // }
}
