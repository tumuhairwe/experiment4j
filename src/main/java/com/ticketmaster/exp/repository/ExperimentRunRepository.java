package com.ticketmaster.exp.repository;

import com.ticketmaster.exp.model.ExperimentRun;

/**
 * This repo should answer the following questions:
 *
 * - Question: How many times has a given experiments have been run?
 * - Usage: ExactCountGuard needs to know how many experiments have been run
 * - Sample query:
 *      SELECT COUNT(*) FROM experiment_run
 *      WHERE experiment_name = :expName
 * - Goal: Stop the experiment is count exceeds threshold
 *
 * - Question: How many mismatches have occured in a given experiment?
 * - Usage: ExactMismatchGuard needs to know how many mismatches have occurred
 * - Sample query:
 *      SELECT COUNT(*) FROM experiment_run
 *      WHERE experiment_name = :expName
 *      AND match_type != MATCH
 */
public class ExperimentRunRepository extends JpaRepository<ExperimentRun> {
}