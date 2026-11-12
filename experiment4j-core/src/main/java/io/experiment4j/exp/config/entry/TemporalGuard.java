package io.experiment4j.exp.config.entry;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * An entry strategy that allows entry only inside a time window. Both ends of
 * the window are included. It has two modes:
 *
 * <ul>
 *   <li><b>Date window</b>: {@code new TemporalGuard(startDateTime, endDateTime)}
 *       "Only run this experiment between startDateTime and endDateTime", i.e. when
 *       {@code startDateTime <= time <= endDateTime}.</li>
 *   <li><b>Time of day on given days</b>: {@code new TemporalGuard(startTime, endTime, days...)}
 *       "Only run this experiment between startTime and endTime on these days", i.e. when
 *       the day is one of {@code days} and {@code startTime <= time of day <= endTime}.
 *       With no days given, Monday to Friday is used.</li>
 * </ul>
 *
 * <p>Windows that cross midnight (e.g. 22:00 to 06:00) are not supported.
 */
public class TemporalGuard implements EntryGuard {
    private static final Set<DayOfWeek> WORKING_DAYS = Collections.unmodifiableSet(EnumSet.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
    ));

    // date-window mode
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    // time-of-day mode
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Set<DayOfWeek> allowedDays;

    /** Allows entry from {@code startDate} to {@code endDate}, both included. */
    public TemporalGuard(LocalDateTime startDate, LocalDateTime endDate) {
        requireOrderedWindow(startDate, endDate);
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = null;
        this.endTime = null;
        this.allowedDays = null;
    }

    /**
     * Allows entry on the given days from {@code startTime} to {@code endTime}, both
     * included. With no days given, Monday to Friday is used.
     * e.g. Monday to Friday, 9am to 5pm: {@code new TemporalGuard(LocalTime.of(9, 0), LocalTime.of(17, 0))}
     */
    public TemporalGuard(LocalTime startTime, LocalTime endTime, DayOfWeek... daysOfWeek) {
        requireOrderedWindow(startTime, endTime);
        this.startDate = null;
        this.endDate = null;
        this.startTime = startTime;
        this.endTime = endTime;
        this.allowedDays = (daysOfWeek == null || daysOfWeek.length == 0)
                ? WORKING_DAYS
                : Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(daysOfWeek)));
    }

    @Override
    public boolean shouldEnter(int totalRuns, int mismatches, LocalDateTime timeToRun) {
        return canEnter(timeToRun);
    }

    /** Whether {@code dateTimeToRun} falls inside this guard's window. */
    public boolean canEnter(LocalDateTime dateTimeToRun) {
        if (startDate != null) {
            return !dateTimeToRun.isBefore(startDate) && !dateTimeToRun.isAfter(endDate);
        }
        LocalTime timeOfDay = dateTimeToRun.toLocalTime();
        return allowedDays.contains(dateTimeToRun.getDayOfWeek())
                && !timeOfDay.isBefore(startTime)
                && !timeOfDay.isAfter(endTime);
    }

    private static <T extends Comparable<? super T>> void requireOrderedWindow(T start, T end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("TemporalGuard needs both a start and an end");
        }
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException(
                    "TemporalGuard start (" + start + ") must not be after its end (" + end + ")");
        }
    }
}
