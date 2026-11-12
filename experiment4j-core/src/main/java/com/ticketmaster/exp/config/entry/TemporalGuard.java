package com.ticketmaster.exp.config.entry;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * An entry strategy that allows entry based on a specific time window.
 * "Only run this experiment between startTime and endTime" i.e. when (currentTime >= startTime && currentTime <= endTime)
 */
public class TemporalGuard implements EntryGuard {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private static final Set<DayOfWeek> WORKING_DAYS = Set.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
    );

    private Set<DayOfWeek> allowedDays = WORKING_DAYS;
    private LocalTime startTime;
    private LocalTime endTime;
    public TemporalGuard(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    // model (Monday-to-FRI, 9am-5pm) == new TemporalGuard(LocalTime.of(9,0), LocalTime.of(17,0), DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
    public TemporalGuard(LocalTime startTime, LocalTime endTime, DayOfWeek... daysOfWeek) {
        this.startTime = startTime;
        this.endTime = endTime;
        if (daysOfWeek != null && daysOfWeek.length > 0) {
            allowedDays = new HashSet<>(Arrays.asList(daysOfWeek));
        }
    }
    @Override
//    public boolean shouldEnter(int totalRuns, int mismatches, LocalDateTime timeToRun) {
//        //boolean x = timeToRun.isAfter(startDate) && timeToRun.isBefore(endDate);
//        return canEnter(timeToRun) && (timeToRun.isAfter(startDate) && timeToRun.isBefore(endDate));
//    }
    public boolean shouldEnter(int totalRuns, int mismatches, LocalDateTime timeToRun) {
        return timeToRun.isAfter(startDate) && timeToRun.isBefore(endDate);
    }
    public boolean canEnter(LocalDateTime dateTimeToRun) {
        boolean dayIsInRange = allowedDays.contains(dateTimeToRun.getDayOfWeek());
        boolean hourIsInRange = false;
        if(startDate != null && endDate != null){
            dayIsInRange = dateTimeToRun.isAfter(startDate) && dateTimeToRun.isBefore(endDate);
        }
        else if(startTime != null && endTime != null){
            LocalTime timeToRun = dateTimeToRun.toLocalTime();
            //hourIsInRange = timeToRun.isAfter(startTime) && timeToRun.isBefore(endTime);
            hourIsInRange = startTime.isAfter(timeToRun) && endTime.isBefore(timeToRun);
        }
        else {
            throw new IllegalStateException("TemporalGuard must have either startDate/endDate or startTime/endTime");
        }

        return dayIsInRange && hourIsInRange;
    }
}
