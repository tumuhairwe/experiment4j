package com.ticketmaster.exp.config.entry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExactCountGuardTest {

    @Test
    @DisplayName("when the exact count is met, the guard should pass")
    public void testExactCountGuard_full() {
        ExactCountGuard guard = new ExactCountGuard(5);
        boolean shouldEnter = guard.shouldEnter(5, 0, null);
        assertTrue(shouldEnter);
    }
    @Test
    @DisplayName("when the experiment is disabled (max runs = 0), the guard should block all entries")
    public void testExactCountGuard_disabled() {
        ExactCountGuard guard = new ExactCountGuard(0);
        boolean shouldEnter = guard.shouldEnter(1, 0, null);
        assertFalse(shouldEnter);
    }
    @Test
    @DisplayName("when the experiment is disabled when there are more attempts than allowed, the guard should block all entries")
    public void testExactCountGuard_tooManyAttempts() {
        ExactCountGuard guard = new ExactCountGuard(10);
        boolean shouldEnter = guard.shouldEnter(20, 0, null);
        assertFalse(shouldEnter);
    }
    @Test
    @DisplayName("when there is room to run the experiment, the guard should pass")
    public void testExactCountGuard_hasRoom() {
        ExactCountGuard guard = new ExactCountGuard(10);
        boolean shouldEnter = guard.shouldEnter(8, 0, null);
        assertTrue(shouldEnter);
    }
}
