package com.ticketmaster.exp;

import lombok.Data;

import java.io.Serializable;
import java.time.Duration;

/**
 * Policy defining the allowed mismatches within a certain duration
 */
@Data
public class MismatchPolicy implements Serializable {
    private Duration duration;
    private int maxMismatches;
}
