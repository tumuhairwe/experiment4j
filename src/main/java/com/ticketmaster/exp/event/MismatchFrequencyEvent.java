package com.ticketmaster.exp.event;

import lombok.Data;

import java.time.temporal.TemporalUnit;

/**
 * InfluxDB event listener should publish events of this type when mismatches are detected.
 * "There are X mismatches in Y time period for experiment Z"
 */
@Data
public class MismatchFrequencyEvent {
    private String experimentName;
    private int mismatchCount;
    private TemporalUnit timeUnit;
    private int count;
}
