package com.ticketmaster.exp.config;

import lombok.Data;

import java.io.Serializable;
import java.time.Period;

/**
 * Represents a single step in a sequence-based ramp-up plan
 * like Canary, Linear, or Exponential.
 * Fields not applicable to a specific strategy will be null.
 */
@Data
public abstract class RampUpStep implements Serializable {
    // Fields for Canary, Exponential, Linear strategies
    //private Duration duration;  // time in days, hours, minutes, seconds, milliseconds, nanos
    private Period period;      // time in days, weeks, months, years

    // increase traffic to this percentage at each step
    private int trafficPercentage;

}