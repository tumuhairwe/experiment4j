package com.ticketmaster.exp.config.entry;

import com.ticketmaster.exp.util.Selectors;

import java.time.LocalDateTime;

/**
 * A guard interface for defining entry conditions for the experiment.
 */
public interface EntryGuard {

    boolean shouldEnter(int totalRuns, int mismatches, LocalDateTime dateTime);
    default EntryGuard or(EntryGuard other){
        return (totalRuns, mismatches, dateTime) ->
                this.shouldEnter(totalRuns, mismatches, dateTime) ||
                        other.shouldEnter(totalRuns, mismatches, dateTime);
    }
    EntryGuard NEVER = (totalRuns, mismatches, dateTime) -> Selectors.NEVER.getAsBoolean();
    EntryGuard ALWAYS = (totalRuns, mismatches, dateTime) -> Selectors.ALWAYS.getAsBoolean();
}
