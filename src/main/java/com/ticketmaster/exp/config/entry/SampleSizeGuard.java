package com.ticketmaster.exp.config.entry;

import com.ticketmaster.exp.Experiment;

import java.time.LocalDateTime;
import java.util.Optional;

public class SampleSizeGuard implements EntryGuard{
    private final String experimentName;
    public SampleSizeGuard(String experimentName) {
        this.experimentName = experimentName;
    }
    @Override
    public boolean shouldEnter(int totalRuns, int mismatches, LocalDateTime dateTime) {
        //0. look up experiment by name
        // ExperimentRecord is a persistent records {String name, int maxSampleSize, ...}
        //Optional<ExperimentRecord> experiment = experimentService.findByName(experimentName);

        //1. check if totalRuns is less than the experiment's maximum sample size
        return false;
    }
}
