/*
 * Copyright 2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.experiment4j.exp.store;

import io.experiment4j.exp.util.Assert;

import java.time.Instant;

/**
 * Where an experiment's rollout stands. Shared by every application instance
 * running the experiment, and changed only through
 * {@link ExperimentStateStore#update}, which records a {@link StateTransition}.
 *
 * <p>{@code version} is managed by the store: it starts at 0 and increases by one
 * on every update, so concurrent writers can't overwrite each other's changes.
 *
 * @param experimentName  name of the experiment; required
 * @param status          lifecycle status; required
 * @param trafficPercent  share of traffic on which the candidate runs, 0-100
 * @param currentStep     index of the current ramp-up step, 0 or more
 * @param stepEnteredAt   when the current step started; required
 * @param killed          whether the kill switch is on: if so, only the control runs
 * @param version         optimistic-locking version, 0 or more; set by the store
 * @param updatedAt       when this state was written; required
 */
public record ExperimentState(
    String experimentName,
    ExperimentStatus status,
    int trafficPercent,
    int currentStep,
    Instant stepEnteredAt,
    boolean killed,
    long version,
    Instant updatedAt) {

  public ExperimentState {
    Assert.hasText(experimentName, "experimentName must be non-null and have text");
    Assert.notNull(status, "status must be non-null");
    Assert.between(trafficPercent, 0, 101);
    if (currentStep < 0) {
      throw new IllegalArgumentException("currentStep must be 0 or more, was " + currentStep);
    }
    Assert.notNull(stepEnteredAt, "stepEnteredAt must be non-null");
    if (version < 0) {
      throw new IllegalArgumentException("version must be 0 or more, was " + version);
    }
    Assert.notNull(updatedAt, "updatedAt must be non-null");
  }

  /** The state of a newly created experiment: {@link ExperimentStatus#DRAFT}, step 0. */
  public static ExperimentState initial(String experimentName, int trafficPercent, Instant now) {
    return new ExperimentState(experimentName, ExperimentStatus.DRAFT, trafficPercent, 0, now, false, 0, now);
  }

  /** The same state with a new status and traffic share, written at {@code now}. */
  public ExperimentState withStatus(ExperimentStatus newStatus, int newTrafficPercent, Instant now) {
    return new ExperimentState(experimentName, newStatus, newTrafficPercent, currentStep,
        stepEnteredAt, killed, version, now);
  }

  /** The same state moved to ramp-up step {@code step} with a new traffic share, entered at {@code now}. */
  public ExperimentState withStep(int step, int newTrafficPercent, Instant now) {
    return new ExperimentState(experimentName, status, newTrafficPercent, step, now, killed, version, now);
  }

  /** The same state with the kill switch on or off, written at {@code now}. */
  public ExperimentState withKilled(boolean newKilled, Instant now) {
    return new ExperimentState(experimentName, status, trafficPercent, currentStep, stepEnteredAt,
        newKilled, version, now);
  }

  /** Whether the candidate should run at all: not killed, a running status and some traffic. */
  public boolean isCandidateRunning() {
    return !killed
        && trafficPercent > 0
        && (status == ExperimentStatus.SHADOW
            || status == ExperimentStatus.RAMPING
            || status == ExperimentStatus.CANDIDATE_PRIMARY);
  }
}
