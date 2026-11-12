package com.ticketmaster.exp.config;

import com.ticketmaster.exp.ExperimentContext;
import com.ticketmaster.exp.config.rampup.DefaultRampUpStrategy;
import com.ticketmaster.exp.config.rampup.DoNothingRampUpStrategy;

import java.time.Period;

public interface RampUpStrategy {
    RampUpStrategy NO_RAMP_UP = new DoNothingRampUpStrategy();
    RampUpStrategy DEFAULT = new DefaultRampUpStrategy(10, 0, Period.ofDays(1));    // increment by 10 % if no mismatches in last day
    int getAdjustment(ExperimentContext context);
}

