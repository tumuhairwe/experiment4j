package com.ticketmaster.exp.config.rampup;

import com.ticketmaster.exp.ExperimentContext;
import com.ticketmaster.exp.config.RampUpStrategy;

import java.time.Duration;

/**
 * Ramp up only if there are X mismatches in the past X [minutes | hours | days].
 * e.g. "Ramp up if there are 0 mismatches in the last 10 minutes"
 * e.g. "Ramp up if there are 5 or fewer mismatches in the last hour"
 * e.g. "Ramp up if there are 20 or fewer mismatches in the last day"
 * e.g. "Ramp up if there are 100 or fewer mismatches in the last week"
 * e.g. "Ramp up if there are 500 or fewer mismatches in the last month"
 * e.g. "Ramp down by 10% if there are 5 or more mismatches in the last 10 minutes" : {duration = 10 minutes, baseline = 5, increment = -10%}
 */
public class MismatchGuageStrategy implements RampUpStrategy {
    private Duration duration;
    private int baseline;
    private int increment;

    // goal is to have a Map<key: trialName | String,
    //                       value: trialResult {TrialType, endTimestamp, Duration}> to be able to query the results by type and time range
    // and answer the question: "how many mismatches were there in the last X [minutes | hours ]?"
    // mismatch = MatchType.EXCEPTION_MISMATCH || MatchType.MISMATCH
    // trialType = TrialType.CONTROL || TrialType.CANDIDATE
    // then use that to determine if we should ramp up or down

    public MismatchGuageStrategy(Duration duration, int baseline, int increment) {
        this.duration = duration;
        this.baseline = baseline;
        this.increment = increment;
    }

    @Override
    public int getAdjustment(ExperimentContext context) {
        //return mismatches <= baseline && duration.compareTo(this.duration) >= 0;
        //return mismatches <= baseline ? increment : 0;
        if(context.getTotalRuns().get() / context.getMismatches().get() >= baseline){
            return increment;
        }
        return 0;
    }
}
