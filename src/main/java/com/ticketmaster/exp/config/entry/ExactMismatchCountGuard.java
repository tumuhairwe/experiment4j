package com.ticketmaster.exp.config.entry;


import java.time.LocalDateTime;

/**
 * Entry guard based on the max number of mismatches.
 * "Don't run this experiment if there are more than X mismatches"
 */
public record ExactMismatchCountGuard(int maxAllowableMismatches) implements EntryGuard {

    @Override
    public boolean shouldEnter(int totalRuns, int mismatches, LocalDateTime dateTime) {
        return mismatches <= maxAllowableMismatches;
    }
}
