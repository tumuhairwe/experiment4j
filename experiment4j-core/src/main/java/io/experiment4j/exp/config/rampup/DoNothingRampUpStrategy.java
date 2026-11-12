package io.experiment4j.exp.config.rampup;

import io.experiment4j.exp.ExperimentContext;
import io.experiment4j.exp.config.RampUpStrategy;

/**
 * A RampUpStrategy that does nothing and always returns 0 adjustment.
 */
public class DoNothingRampUpStrategy implements RampUpStrategy {
    @Override
    public int getAdjustment(ExperimentContext context) {
        return 0;
    }
}
