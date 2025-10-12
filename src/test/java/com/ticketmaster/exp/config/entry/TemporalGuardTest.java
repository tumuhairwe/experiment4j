package com.ticketmaster.exp.config.entry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
