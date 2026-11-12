package io.experiment4j.exp.config.entry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemporalGuardTest {
    @Test
    @DisplayName("When time to run is BEFORE, shouldEnter() returns true")
    public void testTimeToRunIsInWindow() {
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        TemporalGuard guard = new TemporalGuard(lastWeek, today);

        boolean shouldEnter = guard.shouldEnter(0, 0, yesterday);
        assertTrue(shouldEnter);
    }
    @Test
    @DisplayName("When time to run is in the past, shouldEnter() returns false")
    public void testTimeToRunIsOutsideWindow_future() {
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

        TemporalGuard guard = new TemporalGuard(lastWeek, today);

        boolean shouldEnter = guard.shouldEnter(0, 0, tomorrow);
        assertFalse(shouldEnter);
    }
    @Test
    @DisplayName("When time to run is AFTER the window, shouldEnter() returns false")
    public void testTimeToRunIsOutsideWindow_past() {
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

        TemporalGuard guard = new TemporalGuard(lastWeek, yesterday);

        boolean shouldEnter = guard.shouldEnter(0, 0, tomorrow);
        assertFalse(shouldEnter);
    }

    // fixed dates: 2026-11-09 is a Monday, 2026-11-14 a Saturday
    private static final LocalDateTime MONDAY_NOON = LocalDateTime.of(2026, 11, 9, 12, 0);
    private static final LocalDateTime SATURDAY_NOON = LocalDateTime.of(2026, 11, 14, 12, 0);
    private static final LocalTime NINE = LocalTime.of(9, 0);
    private static final LocalTime FIVE_PM = LocalTime.of(17, 0);

    @Test
    @DisplayName("date window includes its start and end")
    public void testDateWindow_inclusiveBounds() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 30, 23, 59);
        TemporalGuard guard = new TemporalGuard(start, end);

        assertTrue(guard.shouldEnter(0, 0, start));
        assertTrue(guard.shouldEnter(0, 0, MONDAY_NOON));
        assertTrue(guard.shouldEnter(0, 0, end));
        assertFalse(guard.shouldEnter(0, 0, start.minusNanos(1)));
        assertFalse(guard.shouldEnter(0, 0, end.plusNanos(1)));
        assertTrue(guard.canEnter(MONDAY_NOON));
    }

    @Test
    @DisplayName("date window ignores the day of the week")
    public void testDateWindow_anyDay() {
        TemporalGuard guard = new TemporalGuard(LocalDateTime.of(2026, 11, 1, 0, 0), LocalDateTime.of(2026, 11, 30, 0, 0));

        assertTrue(guard.shouldEnter(0, 0, SATURDAY_NOON));
    }

    @Test
    @DisplayName("time-of-day window enters inside the hours on an allowed day, including start and end")
    public void testTimeOfDay_insideWindow() {
        TemporalGuard guard = new TemporalGuard(NINE, FIVE_PM, DayOfWeek.MONDAY);
        LocalDate monday = MONDAY_NOON.toLocalDate();

        assertTrue(guard.shouldEnter(0, 0, MONDAY_NOON));
        assertTrue(guard.shouldEnter(0, 0, monday.atTime(NINE)));
        assertTrue(guard.shouldEnter(0, 0, monday.atTime(FIVE_PM)));
        assertTrue(guard.canEnter(MONDAY_NOON));
    }

    @Test
    @DisplayName("time-of-day window does not enter outside the hours")
    public void testTimeOfDay_outsideHours() {
        TemporalGuard guard = new TemporalGuard(NINE, FIVE_PM, DayOfWeek.MONDAY);
        LocalDate monday = MONDAY_NOON.toLocalDate();

        assertFalse(guard.shouldEnter(0, 0, monday.atTime(NINE).minusNanos(1)));
        assertFalse(guard.shouldEnter(0, 0, monday.atTime(FIVE_PM).plusNanos(1)));
        assertFalse(guard.shouldEnter(0, 0, monday.atTime(3, 0)));
    }

    @Test
    @DisplayName("time-of-day window does not enter on other days")
    public void testTimeOfDay_otherDay() {
        TemporalGuard guard = new TemporalGuard(NINE, FIVE_PM, DayOfWeek.MONDAY);

        assertFalse(guard.shouldEnter(0, 0, MONDAY_NOON.plusDays(1)));
    }

    @Test
    @DisplayName("time-of-day window with no days given defaults to Monday to Friday")
    public void testTimeOfDay_defaultsToWorkingDays() {
        TemporalGuard guard = new TemporalGuard(NINE, FIVE_PM);

        for (int day = 0; day < 5; day++) {
            assertTrue(guard.shouldEnter(0, 0, MONDAY_NOON.plusDays(day)), "weekday " + day);
        }
        assertFalse(guard.shouldEnter(0, 0, SATURDAY_NOON));
        assertFalse(guard.shouldEnter(0, 0, SATURDAY_NOON.plusDays(1)));
    }

    @Test
    @DisplayName("missing bounds are rejected when the guard is created")
    public void testRejectsMissingBounds() {
        assertThrows(IllegalArgumentException.class, () -> new TemporalGuard((LocalDateTime) null, MONDAY_NOON));
        assertThrows(IllegalArgumentException.class, () -> new TemporalGuard(MONDAY_NOON, (LocalDateTime) null));
        assertThrows(IllegalArgumentException.class, () -> new TemporalGuard(null, FIVE_PM));
        assertThrows(IllegalArgumentException.class, () -> new TemporalGuard(NINE, null));
    }

    @Test
    @DisplayName("an end before the start is rejected, including windows that cross midnight")
    public void testRejectsReversedWindow() {
        assertThrows(IllegalArgumentException.class, () -> new TemporalGuard(MONDAY_NOON, MONDAY_NOON.minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> new TemporalGuard(LocalTime.of(22, 0), LocalTime.of(6, 0)));
    }

    @Test
    @DisplayName("a window can start and end at the same moment")
    public void testSingleInstantWindow() {
        assertTrue(new TemporalGuard(MONDAY_NOON, MONDAY_NOON).shouldEnter(0, 0, MONDAY_NOON));
        assertTrue(new TemporalGuard(NINE, NINE, DayOfWeek.MONDAY)
                .shouldEnter(0, 0, MONDAY_NOON.toLocalDate().atTime(NINE)));
    }
}
