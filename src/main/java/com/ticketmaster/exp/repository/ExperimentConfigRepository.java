package com.ticketmaster.exp.repository;

import com.google.common.collect.Range;
import com.ticketmaster.exp.config.ExperimentConfig;
import com.ticketmaster.exp.config.ScienceConfig;

public class ExperimentConfigRepository {
    private final ScienceConfig scienceConfig;

    public ExperimentConfigRepository(ScienceConfig scienceConfig) {
        this.scienceConfig = scienceConfig;
    }

    /**
     * Find the maximum allowable mismatches for  given experiment & the minimum
     * @param experimentName
     * @return Range<Integer> representing min and max mismatch thresholds (whenMin-> rampUp, whenMax->rampDown)
     */
    public Range<Integer> findMismatchThreshold(String experimentName) {
        // read science.experiment.mismatch.threshold from Experiment from config (JSON or DB)
        ExperimentConfig config = scienceConfig.getExperimentConfig();
        config.getMismatchThresholdForRampUp();
        return Range.atMost(100);
    }
}
