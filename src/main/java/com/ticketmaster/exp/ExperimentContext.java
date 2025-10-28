package com.ticketmaster.exp;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
/**
 * Tracks the context of an experiment, including total runs, mismatches, and mismatch rate.
 * Should have all the state needed to make decisions about running experiments, ramping up/down, etc.
 */
public class ExperimentContext {
    private AtomicInteger totalRuns;
    private AtomicInteger mismatches;
    private AtomicInteger mismatchRate;
}
