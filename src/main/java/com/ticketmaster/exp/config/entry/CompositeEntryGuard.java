package com.ticketmaster.exp.config.entry;

import java.time.LocalDateTime;

/**
 * Default entry guard that combines max runs and max mismatches guards.
 * "Don't run this experiment if it has run more than X times or there are more than Y mismatches"
 */
public class CompositeEntryGuard implements EntryGuard {
    private EntryGuard guards;

    public CompositeEntryGuard(EntryGuard guard) {
        this.guards = guard;
    }

    @Override
    public boolean shouldEnter(int totalRuns, int mismatches, LocalDateTime dateTime) {
        return guards.shouldEnter(totalRuns, mismatches, dateTime);
    }
}
