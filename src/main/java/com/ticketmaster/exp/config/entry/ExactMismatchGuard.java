package com.ticketmaster.exp.config.entry;


import java.time.LocalDateTime;

/**
 * Entry guard based on the max number of mismatches.
 * "Don't run this experiment if there are more than X mismatches"
 */
public class ExactMismatchGuard implements EntryGuard {
    private final int maxAllowableMismatches;

    public ExactMismatchGuard(int maxAllowableMismatches) {
        this.maxAllowableMismatches = maxAllowableMismatches;
    }

    @Override
    public boolean shouldEnter(int totalRuns, int mismatches, LocalDateTime dateTime) {
        return mismatches <= maxAllowableMismatches;
    }
}
