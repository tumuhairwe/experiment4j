package com.ticketmaster.exp.config.entry;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
/**
 * Default entry guard that combines max runs and max mismatches guards.
 * "Don't run this experiment if it has run more than X times or there are more than Y mismatches"
 */
public class CompositeEntryGuard implements EntryGuard {
    private List<EntryGuard> guards;

    public CompositeEntryGuard(List<EntryGuard> guards) {
        this.guards = guards;
    }

    @Override
    public boolean shouldEnter(int totalRuns, int mismatches, LocalDateTime dateTime) {
        return guards.stream().allMatch(g -> g.shouldEnter(totalRuns, mismatches, dateTime));
    }

}
