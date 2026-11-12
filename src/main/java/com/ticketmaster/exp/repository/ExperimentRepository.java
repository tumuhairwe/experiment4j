package com.ticketmaster.exp.repository;

import com.google.common.collect.Range;
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

    /**
     * Find the maximum allowable mismatches for  given experiment & the minimum
     * @param experimentName
     * @return Range<Integer> representing min and max mismatch thresholds (whenMin-> rampUp, whenMax->rampDown)
     */
    public Range<Integer> findMismatchThreshold(String experimentName) {
        // read science.experiment.mismatch.threshold from Experiment from config (JSON or DB)
        return Range.atMost(100);
    }
}
