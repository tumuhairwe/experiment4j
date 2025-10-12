package com.ticketmaster.exp.config.entry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DurationGuardTest {

    @Test
    @DisplayName("when an experiment starts within the allowed time, the guard should pass")
    public void testDurationGuard() {
        LocalDateTime now = LocalDateTime.now();
        Duration oneWeekDuration = Duration.ofDays(7); // Use Duration.ofHours(1) if you want to represent hours instead of days

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        DurationGuard guard = DurationGuard.of(oneWeekDuration, now);


        boolean shouldEnter = guard.shouldEnter(0, 0, yesterday );
        assertTrue(shouldEnter, "Guard should allow entry if current time is within allowed window");
    }
    @Test
    @DisplayName("when a test starts AFTER the allowed time, it should fail")
    public void testDurationGuard_future() {
        LocalDateTime now = LocalDateTime.now();
        Duration oneWeekDuration = Duration.ofDays(7); // Use Duration.ofHours(1) if you want to represent hours instead of days

        LocalDateTime eightDaysFromNow = LocalDateTime.now().plusDays(8);
        DurationGuard guard = DurationGuard.of(oneWeekDuration, now);


        boolean shouldEnter = guard.shouldEnter(0, 0, eightDaysFromNow );
        assertFalse(shouldEnter, "Guard should not allow entry after duration has passed");
    }
    @Test
    @DisplayName("when a test starts BEFORE the allowed time, it should fail")
    public void testDurationGuard_past() {
        LocalDateTime now = LocalDateTime.now();
        Duration oneWeekDuration = Duration.ofDays(7); // Use Duration.ofHours(1) if you want to represent hours instead of days

        LocalDateTime lastMonth = LocalDateTime.now().plusMonths(1);
        DurationGuard guard = DurationGuard.of(oneWeekDuration, now);


        boolean shouldEnter = guard.shouldEnter(0, 0, lastMonth );
        assertFalse(shouldEnter, "Guard should not allow entry after duration has passed");
    }
}
