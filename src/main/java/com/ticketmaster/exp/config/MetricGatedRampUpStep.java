package com.ticketmaster.exp.config;

import lombok.Data;

import java.io.Serializable;

/**
 * Ramp-up step that proceeds only if a specified metric meets a defined condition.
 * For example, ramp up only if the error rate metric is below a certain threshold.
 * Fields:
 * - metricName: Name of the metric to evaluate (e.g., "error_rate").
 * - operator: Comparison operator (e.g., LESS_THAN, GREATER_THAN).
 * - threshold: The threshold value for the metric.
 *
 */
@Data
public class MetricGatedRampUpStep extends RampUpStep implements Serializable {
    /**
     * Name of the metric to evaluate (e.g., "error_rate").
     */
    private String metricName;
    private Operator comparator;

    /**
     * The threshold value for the metric.
     */
    private double threshold;
}
