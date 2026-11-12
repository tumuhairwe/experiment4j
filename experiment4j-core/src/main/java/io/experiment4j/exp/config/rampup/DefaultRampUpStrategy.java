package io.experiment4j.exp.config.rampup;

import io.experiment4j.exp.ExperimentContext;
import io.experiment4j.exp.config.RampUpStrategy;

import java.time.Period;

public class DefaultRampUpStrategy implements RampUpStrategy {
    private int increment;
    private int threshold;
    private Period period;

    public DefaultRampUpStrategy(int increment, int threshold, Period period) {
        this.increment = increment;
        this.threshold = threshold;
        this.period = period;
    }

    /**
     * Ramps up by {@code increment} once the experiment has run at least once and
     * has had no more than {@code threshold} mismatches; otherwise no change.
     */
    @Override
    public int getAdjustment(ExperimentContext context) {
        int totalRuns = context.getTotalRuns().get();
        int mismatches = context.getMismatches().get();
        if (totalRuns > 0 && mismatches <= threshold) {
            return increment;
        }
        return 0;
    }

    public int getIncrement() {
        return increment;
    }
}
