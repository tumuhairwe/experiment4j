package com.ticketmaster.exp.config;

import lombok.Data;
import java.io.Serializable;

/**
 * Represents a health metric threshold used in the
 * Metric-Gated ramp-up strategy.
 */
@Data
public class HealthMetric implements Serializable {
    private String metricName;
    private double threshold;
    private String operator; // e.g., "LESS_THAN", "GREATER_THAN_OR_EQUAL_TO"
}
