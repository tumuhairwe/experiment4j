package io.experiment4j.exp.config.entry;

import java.time.LocalDateTime;

/**
 * An entry strategy that allows a Trial based on a maximum number of runs.
 * "Only run this experiment at most X times"
 * <p>{@code totalRuns} is the number of runs already completed, so a trial may
 * enter while {@code totalRuns < maximumAllowableRuns}. To disable an experiment,
 * set maximumAllowableRuns to 0.
 */
public class ExactCountGuard implements EntryGuard {
    private final int maximumAllowableRuns;
    public ExactCountGuard(int maximumAllowableRuns) {
        this.maximumAllowableRuns = maximumAllowableRuns;
    }
    @Override
    public boolean shouldEnter(int totalRuns, int mismatches, LocalDateTime dateTime) {
        return totalRuns < maximumAllowableRuns;
    }
}
