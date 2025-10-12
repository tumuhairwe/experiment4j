package com.ticketmaster.exp.config;

import java.io.Serializable;
import java.time.Duration;

public class RampUpConfig implements Serializable {
    // ramp up only if there are X mismatches in the past duration (e.g., at most 5 mismatches in the past 24 hours)
    private Duration duration = Duration.ofHours(24);
    private int minimumMismatchThreshold = 5;

    public Duration getDuration() {
        return duration;
    }
    public void setDuration(Duration duration) {
        this.duration = duration;
    }
    public int getMinimumMismatchThreshold() {
        return minimumMismatchThreshold;
    }
    public void setMinimumMismatchThreshold(int minimumMismatchThreshold) {
        this.minimumMismatchThreshold = minimumMismatchThreshold;
    }
}
