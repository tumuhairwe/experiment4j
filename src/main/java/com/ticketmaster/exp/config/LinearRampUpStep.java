package com.ticketmaster.exp.config;

import lombok.Data;

import java.io.Serializable;
@Data
public class LinearRampUpStep extends RampUpStep implements Serializable {
    // ramp up only if there are
    // at_least: minRuns
    // AND
    // at_most: X maxMismatches or less in the past duration (e.g., at most 5 mismatches in the past 24 hours)

    // minimum number of runs to consider ramping up
    private int minRuns;
    private int maxMismatches;
}
