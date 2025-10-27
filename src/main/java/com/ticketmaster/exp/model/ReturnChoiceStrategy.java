package com.ticketmaster.exp.model;

/**
 * Defines the strategy for choosing which result to return after an experiment runs.
 */
public enum ReturnChoiceStrategy {
    /**
     * Always return the result from the control path.
     */
    ALWAYS_CONTROL,

    /**
     * Always return the result from the candidate path.
     */
    ALWAYS_CANDIDATE,

    /**
     * Return the result from the path that completed fastest.
     */
    FIND_FASTEST,

    /**
     * Return the result based on a predefined "best" outcome.
     * Logic for determining "best" is defined elsewhere.
     */
    FIND_BEST,

    /**
     * Return the candidate's result only when a specific condition is met.
     * The condition is defined in the associated metadata.
     */
    CANDIDATE_WHEN
}
