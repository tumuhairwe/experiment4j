package com.ticketmaster.exp.config.entry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExactMismatchGuardTest {
    @Test
    @DisplayName("when the exact mismatch count is met, the guard should pass")
    public void testExactMismatchGuard_inBounds() {
        ExactMismatchGuard guard = new ExactMismatchGuard(3);

        boolean shouldEnter1 = guard.shouldEnter(0, 3, null);

        assertTrue(shouldEnter1);
    }
    @Test
    @DisplayName("when the exact mismatch count is exceeded, the guard should block all entries")
    public void testExactMismatchGuard_zeroAllowed() {
        ExactMismatchGuard guard = new ExactMismatchGuard(0);

        boolean shouldEnter1 = guard.shouldEnter(0, 0, null);
        assertTrue(shouldEnter1);

        boolean shouldEnter2 = guard.shouldEnter(0, 1, null);
        assertFalse(shouldEnter2);
    }

    @Test
    @DisplayName("when the exact mismatch count is exceeded, the guard should block all entries")
    public void testExactMismatchGuard_outOfBoundsLeft() {
        ExactMismatchGuard guard = new ExactMismatchGuard(3);
        boolean shouldEnter3 = guard.shouldEnter(0, 4, null);
        assertFalse(shouldEnter3);
    }
}
