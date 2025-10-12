package com.ticketmaster.exp.config.rampup;

import com.ticketmaster.exp.ExperimentContext;
import com.ticketmaster.exp.config.RampUpStrategy;

/**
 * A RampUpStrategy that does nothing and always returns 0 adjustment.
 */
public class DoNothingRampUpStrategy implements RampUpStrategy {
    @Override
    public int getAdjustment(ExperimentContext context) {
        return 0;
    }
}
