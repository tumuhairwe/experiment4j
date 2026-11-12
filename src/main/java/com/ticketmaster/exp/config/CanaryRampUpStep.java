package com.ticketmaster.exp.config;

import com.ticketmaster.exp.ExperimentContext;
import lombok.Data;

import java.io.Serializable;
import java.time.Period;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;

@Data
public class CanaryRampUpStep implements Serializable {
    private String ringName;
    private String description;
    private Period period; // time in days, weeks, months, years
    private Integer trafficPercentage;

    private BiPredicate<ExperimentContext, ExperimentConfig> evaluator;   // function to evaluate if the ramp-up can proceed
    public CanaryRampUpStep() {
        evaluator = (context, experimentConfig) -> {
            AtomicInteger mismatches = context.getMismatches();
            AtomicInteger totalRuns = context.getTotalRuns();
            if (totalRuns.get() >= experimentConfig.getMinRunsForRampUp()) {
                double mismatchRate = (double) mismatches.get() / totalRuns.get();
                return mismatchRate <= ((double) experimentConfig.getMismatchThresholdForRampUp() / 100);
            }
            return false;
        };
    }
}
