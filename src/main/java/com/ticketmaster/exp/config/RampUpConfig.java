package com.ticketmaster.exp.config;

import com.ticketmaster.exp.MismatchPolicy;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RampUpConfig implements Serializable {

    private boolean enabled = true;
    private RampUpStrategyType strategy;
    /**
     * "Don't ramp up if there are more than X mismatches in the last Y duration"
     */
    private MismatchPolicy mismatchPolicy;

    // steps in the ramp up depend on rampUp strategy
    // for CANARY strategy, steps are of type CanaryRampUpStep
    // for MetricGates strategy, steps are of type MetricGatesRampUpStep
    // for TimeBased strategy, steps are of type TimeBasedRampUpStep

    // for Linear strategy, steps are of type LinearRampUpStep
    /**
     * After X days/hours/minutes of successful experiment runs, increase traffic by Y%
     *
     * Linear: After X days/hours/minutes (duration), $TRAFFIC_VOLUME should be increased by $PERCENTAGE_INCREMENT
     * Gradual: Increase traffic by ($EXACT_TRAFFIC_INCREMENT) every ($duration)
     * Canary: Given a function(input) -> CANARY_RING_NAME, after X durationDays expose users in CANARY_RING_NAME to the candidate
     *
     * == Canary: After X days/hours/minutes (duration), increase traffic to (traffic percentage) if mismatches in the duration are <= (max mismatches) and runs >= (min runs)
     */
    private List<CanaryRampUpStep> canarySteps;
    private List<LinearRampUpStep> linearSteps;
    private List<MetricGatedRampUpStep> metricSteps;
    private List<ProgressiveRampUpStep> progressiveSteps;
    private List<ExponentialRampUpStep> exponentialSteps;
}
