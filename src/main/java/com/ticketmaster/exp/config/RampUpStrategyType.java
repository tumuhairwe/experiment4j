package com.ticketmaster.exp.config;

public enum RampUpStrategyType {
    /**
     * Gradually increases the percentage of traffic exposed to the experiment
     * in equal increments over a specified duration. For example, you might start
     * at 10% and increase by 10% every hour until reaching 100%.
     */
    PROGRESSIVE,

    /**
     * Uses named "Rings" for targeted rollouts before moving to a general percentage
     * of traffic. For example, you might have a "Canary" ring that is 5% of traffic,
     * then a "Beta" ring that is 20% of traffic, before going to 50% and then 100%.
     * This allows for more controlled rollouts to specific user groups or segments.
     * Each ring must have a CanaryRampUpStep defined.
     * Each step defines
     * - the ring name and
     * - period in days to stay at that ring before moving to the next step.
     * - a predicate that determines if the step can be executed (should take ExperimentContext + experimentName)
     */
    CANARY,

    /**
     * Increases traffic exposure based predefined metrics
     * e.g.
     * - candidate_error_rate < 0.01 (metricName=candidate_error_rate, threshold=0.01, comparison=LESS_THAN)
     * - latency_p95 <= 100ms  (metricName=latency_p95, threshold=100, comparison=LESS_THAN_OR_EQUAL)
     * - mismatch_percentage < 5% (metricName=mismatch_percentage, threshold=5, comparison=LESS_THAN)
     */
    METRIC_GATED,

    /**
     * No automatic ramp-up. User must manually adjust traffic receiving each variation (e.g. control = 20, candidate = 80).
     */
    MANUAL
}
