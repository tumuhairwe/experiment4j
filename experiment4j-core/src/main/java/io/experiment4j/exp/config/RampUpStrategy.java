package io.experiment4j.exp.config;

import io.experiment4j.exp.ExperimentContext;
import io.experiment4j.exp.config.rampup.DefaultRampUpStrategy;
import io.experiment4j.exp.config.rampup.DoNothingRampUpStrategy;

import java.time.Period;

public interface RampUpStrategy {
    RampUpStrategy NO_RAMP_UP = new DoNothingRampUpStrategy();
    RampUpStrategy DEFAULT = new DefaultRampUpStrategy(10, 0, Period.ofDays(1));    // increment by 10 % if no mismatches in last day
    int getAdjustment(ExperimentContext context);
}

