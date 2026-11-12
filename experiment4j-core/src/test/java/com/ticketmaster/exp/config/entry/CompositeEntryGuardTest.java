package com.ticketmaster.exp.config.entry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompositeEntryGuardTest {
    @Test
    @DisplayName("A composite guard with a temporal and duration guard should PAST when both conditions are met")
    public void testTemporalAndDuration(){
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime today = LocalDateTime.now();
        List<EntryGuard> list = new ArrayList<>();
        list.add(new TemporalGuard(yesterday, tomorrow));
        list.add(new ExactMismatchGuard(10));

        CompositeEntryGuard guard = new CompositeEntryGuard(list);
        boolean shouldEnter = guard.shouldEnter(50, 5, today);
        assertTrue(shouldEnter);
    }
    @Test
    @DisplayName("A composite guard with a temporal and duration guard should FAIL when 1 of conditions are not met")
    public void testTemporalAndDuration_false(){
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime today = LocalDateTime.now();
        List<EntryGuard> list = new ArrayList<>();
        list.add(new TemporalGuard(yesterday, today));
        list.add(new ExactMismatchGuard(10));

        CompositeEntryGuard guard = new CompositeEntryGuard(list);
        boolean shouldEnter = guard.shouldEnter(50, 5, tomorrow);
        assertFalse(shouldEnter);
    }
}
