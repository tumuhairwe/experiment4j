package com.ticketmaster.exp.config.rampup;

import com.ticketmaster.exp.ExperimentContext;
import com.ticketmaster.exp.config.RampUpStrategy;

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

    @Override
    public int getAdjustment(ExperimentContext context) {
        //return totalRuns >= 100 && mismatches == 0 && duration.toMinutes() >= 10;
        if(context.getTotalRuns().get() / context.getMismatches().get() >= threshold){
            return increment;
        }
        return 0;
    }

    public int getIncrement() {
        return increment;
    }
}
