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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExperimentStateTest {

  private static final Instant T0 = Instant.parse("2026-11-01T00:00:00Z");
  private static final Instant T1 = T0.plusSeconds(60);

  @Test
  @DisplayName("initial() is a DRAFT at step 0 and version 0, not killed")
  public void testInitial() {
    ExperimentState state = ExperimentState.initial("exp", 5, T0);

    assertEquals(new ExperimentState("exp", ExperimentStatus.DRAFT, 5, 0, T0, false, 0, T0), state);
  }

  @Test
  @DisplayName("withStatus changes status, traffic and update time, keeping the step")
  public void testWithStatus() {
    ExperimentState state = ExperimentState.initial("exp", 0, T0).withStep(2, 10, T0);

    ExperimentState next = state.withStatus(ExperimentStatus.RAMPING, 20, T1);

    assertEquals(new ExperimentState("exp", ExperimentStatus.RAMPING, 20, 2, T0, false, 0, T1), next);
  }

  @Test
  @DisplayName("withStep moves to a new step entered now")
  public void testWithStep() {
    ExperimentState next = ExperimentState.initial("exp", 0, T0).withStep(3, 40, T1);

    assertEquals(new ExperimentState("exp", ExperimentStatus.DRAFT, 40, 3, T1, false, 0, T1), next);
  }

  @Test
  @DisplayName("withKilled switches the kill switch")
  public void testWithKilled() {
    ExperimentState killed = ExperimentState.initial("exp", 50, T0).withKilled(true, T1);

    assertTrue(killed.killed());
    assertEquals(T1, killed.updatedAt());
    assertEquals(T0, killed.stepEnteredAt());
    assertFalse(killed.withKilled(false, T1).killed());
  }

  @ParameterizedTest
  @EnumSource(value = ExperimentStatus.class, names = {"SHADOW", "RAMPING", "CANDIDATE_PRIMARY"})
  @DisplayName("the candidate runs in a running status with traffic, unless killed")
  public void testCandidateRunning(ExperimentStatus status) {
    ExperimentState running = ExperimentState.initial("exp", 10, T0).withStatus(status, 10, T1);

    assertTrue(running.isCandidateRunning());
    assertFalse(running.withKilled(true, T1).isCandidateRunning());
    assertFalse(running.withStatus(status, 0, T1).isCandidateRunning());
  }

  @ParameterizedTest
  @EnumSource(value = ExperimentStatus.class, names = {"DRAFT", "PAUSED", "ROLLED_BACK", "COMPLETED"})
  @DisplayName("the candidate does not run in a stopped status")
  public void testCandidateNotRunning(ExperimentStatus status) {
    assertFalse(ExperimentState.initial("exp", 100, T0).withStatus(status, 100, T1).isCandidateRunning());
  }

  @Test
  @DisplayName("invalid values are rejected")
  public void testValidation() {
    assertThrows(IllegalArgumentException.class, () -> new ExperimentState("", ExperimentStatus.DRAFT, 0, 0, T0, false, 0, T0));
    assertThrows(IllegalArgumentException.class, () -> new ExperimentState("exp", null, 0, 0, T0, false, 0, T0));
    assertThrows(IllegalArgumentException.class, () -> new ExperimentState("exp", ExperimentStatus.DRAFT, 101, 0, T0, false, 0, T0));
    assertThrows(IllegalArgumentException.class, () -> new ExperimentState("exp", ExperimentStatus.DRAFT, 0, -1, T0, false, 0, T0));
    assertThrows(IllegalArgumentException.class, () -> new ExperimentState("exp", ExperimentStatus.DRAFT, 0, 0, null, false, 0, T0));
    assertThrows(IllegalArgumentException.class, () -> new ExperimentState("exp", ExperimentStatus.DRAFT, 0, 0, T0, false, -1, T0));
    assertThrows(IllegalArgumentException.class, () -> new ExperimentState("exp", ExperimentStatus.DRAFT, 0, 0, T0, false, 0, null));
  }
}
