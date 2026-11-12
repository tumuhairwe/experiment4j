package com.ticketmaster.exp.config.entry;

import java.time.LocalDateTime;
import java.time.Period;

/**
 * Entry guard based on the relative frequency of mismatches to total runs.
 * "Don't run this experiment if the mismatch frequency exceeds X% in the past Y period/TimeUnits"
 */
public class RelativeMismatchFrequency implements EntryGuard{
    private final Period durationPeriod;
    private final double maxMismatchFrequency;

    public RelativeMismatchFrequency(Period durationPeriod, double maxMismatchFrequency) {
        if(maxMismatchFrequency < 0.0 || maxMismatchFrequency > 1.0){
            throw new IllegalArgumentException("maxMismatchFrequency must be between 0.0 and 1.0");
        }
        this.maxMismatchFrequency = maxMismatchFrequency;
        this.durationPeriod = durationPeriod;
    }

    /**
     * Decides whether to enter the experiment based on the mismatch frequency in a given period.
     */
    @Override
    public boolean shouldEnter(int totalRuns, int mismatches, LocalDateTime dateTime) {
        if(totalRuns == 0){
            return true; // No runs yet, so no mismatches
        }
        double currentMismatchFrequency = (double)mismatches / totalRuns;
        return currentMismatchFrequency <= maxMismatchFrequency;
    }
}
