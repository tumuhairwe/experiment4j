package com.ticketmaster.exp.publish;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatternDurationNamerTest {
    @Test
    @DisplayName("PatternDurationNamer formats CANDIDATE duration correctly")
    public void testCandidate() {
        PatternDurationNamer namer = PatternDurationNamer.from("exp.%s.trial.type.%s.dur");
        String name = namer.name("org_team_feature_hypothesis", DurationNamer.DurationType.CANDIDATE);
        assertEquals("exp.org_team_feature_hypothesis.trial.type.candidate.dur", name);
    }
    @Test
    @DisplayName("PatternDurationNamer handles CONTROL duration correctly")
    public void testControl() {
        PatternDurationNamer namer = PatternDurationNamer.from("exp.%s.trial.type.%s.dur");
        String name = namer.name("org_team_feature_hypothesis", DurationNamer.DurationType.CONTROL);
        assertEquals("exp.org_team_feature_hypothesis.trial.type.control.dur", name);
    }
    @Test
    @DisplayName("PatternDurationNamer handles CONTROL duration correctly")
    public void testImprovement() {
        PatternDurationNamer namer = PatternDurationNamer.from("exp.%s.trial.type.%s.dur");
        String name = namer.name("org_team_feature_hypothesis", DurationNamer.DurationType.IMPROVEMENT);
        assertEquals("exp.org_team_feature_hypothesis.trial.type.improvement.dur", name);
    }
}
