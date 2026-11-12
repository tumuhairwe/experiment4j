package com.ticketmaster.exp.config;

import com.ticketmaster.exp.config.entry.EntryGuard;

import java.io.Serializable;
import java.time.LocalDate;

public class ExperimentConfig implements Serializable {
    private String key;
    private LocalDate startDate;
    private LocalDate endDate;
    private RampUpConfig rampUpConfig;
    private EntryGuard entryGuard;

    private int initialTrafficPercentage;
    private int maxTrafficPercentage;

    private int minRunsForRampUp;
    private int mismatchThresholdForRampUp;

    private ComparisonStrategy comparisonStrategy;

}
