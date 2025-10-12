package com.ticketmaster.exp.config.entry;

import java.time.LocalDateTime;

/**
 * Entry guard based on the frequency of mismatches.
 * "Don't run this experiment if the mismatch frequency exceeds X%"
 */
public class MismatchFrequencyGuard implements EntryGuard{
    private final double maxMismatchFrequency;

    public MismatchFrequencyGuard(double maxMismatchFrequency) {
        if (maxMismatchFrequency < 0.0 || maxMismatchFrequency > 1.0) {
            throw new IllegalArgumentException("maxMismatchFrequency must be between 0.0 and 1.0");
        }
        this.maxMismatchFrequency = maxMismatchFrequency;
    }

    @Override
    public boolean shouldEnter(int totalRuns, int mismatches, LocalDateTime dateTime) {
        if (totalRuns == 0) {
            return true; // No runs yet, so allow entry
        }
        double currentMismatchFrequency = (double) mismatches / totalRuns;
        return currentMismatchFrequency <= maxMismatchFrequency;
    }
}
