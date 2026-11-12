package io.experiment4j.exp;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
/**
 * Tracks the context of an experiment, including total runs, mismatches, and mismatch rate.
 * Should have all the state needed to make decisions about running experiments, ramping up/down, etc.
 */
public class ExperimentContext {
    private AtomicInteger totalRuns = new AtomicInteger();
    private AtomicInteger mismatches = new AtomicInteger();
    private AtomicInteger mismatchRate = new AtomicInteger();
}
