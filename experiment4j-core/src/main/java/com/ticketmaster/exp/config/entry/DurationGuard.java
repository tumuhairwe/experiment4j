package com.ticketmaster.exp.config.entry;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;

/**
 * Entry guard based on a specific Period (e.g. run for [X minutes, hours, days] starting at startTime).
 */
public class DurationGuard implements EntryGuard {
    private TemporalAmount durationAmount; // can be either Period (of days/weeks/months/year) or Duration (of hours/minutes/seconds)
    private final LocalDateTime startDateTime;

    // Example:
    // GIVEN: "This experiment should run for 7 days starting tomorrow". After that the guard should return false.
    // THEN
     //var guard = new DurationGuard(Period.ofDays(7), LocalDateTime.now().plusDays(1));
    // boolean shouldEnter = guard.shouldEnter(0, 0, LocalDateTime.now().plusDays(8));
    // SHOULD: shouldEnter == false
    private DurationGuard(TemporalAmount durationPeriod, LocalDateTime startDateTime) {
        this.durationAmount = durationPeriod;
        this.startDateTime = startDateTime; // time to start the experiment
    }

    public static DurationGuard of(TemporalAmount durationAmount, LocalDateTime startTime) {
        return new DurationGuard(durationAmount, startTime);
    }

    @Override
    public boolean shouldEnter(int totalRuns, int mismatches, LocalDateTime dateTime) {
        LocalDateTime currenTime = startDateTime.plus(durationAmount);
        return currenTime.isAfter(dateTime);
    }
}
