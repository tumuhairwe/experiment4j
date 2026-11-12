package com.ticketmaster.exp.config;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ExperimentConfig implements Serializable {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private RampUpConfig rampUpConfig;
    private String entryGuardClassName; // e.g. "NEVER", "ALWAYS" or "com.ticketmaster.exp.entry.DefaultEntryGuard"

    // required if entry is a SampleSizeGuard
    private int maxSampleSize;
    private int initialTrafficPercentage;

    // the maximum traffic percentage to ramp up to, should be <= 100
    private int maxTrafficPercentage;

    // the minimum number of runs required to consider ramping up
    private int minRunsForRampUp;

    // the minimum number of mismatches allowed to consider ramping up

    private int mismatchThresholdForRampUp;
    private int maxMismatchThresholdForRampDown;

    private ComparisonStrategy comparisonStrategy;

    public boolean isActive(){
        LocalDateTime now = LocalDateTime.now();
        return (startDateTime != null && now.isAfter(startDateTime)) &&
               (endDateTime != null && now.isBefore(endDateTime));
    }
}
