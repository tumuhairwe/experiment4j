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

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateTransitionTest {

  private static final Instant T0 = Instant.parse("2026-11-01T00:00:00Z");
  private static final Instant T1 = T0.plusSeconds(60);

  @Test
  @DisplayName("between() records the status and traffic before and after, timed at the new state")
  public void testBetween() {
    ExperimentState current = ExperimentState.initial("exp", 0, T0);
    ExperimentState next = current.withStatus(ExperimentStatus.SHADOW, 5, T1);

    StateTransition transition = StateTransition.between(current, next, TransitionReason.MANUAL, "alice", "kick-off");

    assertEquals(new StateTransition("exp", T1, ExperimentStatus.DRAFT, ExperimentStatus.SHADOW, 0, 5,
        TransitionReason.MANUAL, "alice", "kick-off"), transition);
  }

  @Test
  @DisplayName("between() rejects states of different experiments")
  public void testBetweenDifferentExperiments() {
    ExperimentState a = ExperimentState.initial("a", 0, T0);
    ExperimentState b = ExperimentState.initial("b", 0, T0);

    assertThrows(IllegalArgumentException.class, () -> StateTransition.between(a, b, TransitionReason.MANUAL, "alice", null));
    assertThrows(IllegalArgumentException.class, () -> StateTransition.between(null, b, TransitionReason.MANUAL, "alice", null));
    assertThrows(IllegalArgumentException.class, () -> StateTransition.between(a, null, TransitionReason.MANUAL, "alice", null));
  }

  @Test
  @DisplayName("invalid values are rejected; detail is optional")
  public void testValidation() {
    ExperimentStatus d = ExperimentStatus.DRAFT;
    TransitionReason m = TransitionReason.MANUAL;

    assertThrows(IllegalArgumentException.class, () -> new StateTransition(null, T0, d, d, 0, 0, m, "a", null));
    assertThrows(IllegalArgumentException.class, () -> new StateTransition("exp", null, d, d, 0, 0, m, "a", null));
    assertThrows(IllegalArgumentException.class, () -> new StateTransition("exp", T0, null, d, 0, 0, m, "a", null));
    assertThrows(IllegalArgumentException.class, () -> new StateTransition("exp", T0, d, null, 0, 0, m, "a", null));
    assertThrows(IllegalArgumentException.class, () -> new StateTransition("exp", T0, d, d, -1, 0, m, "a", null));
    assertThrows(IllegalArgumentException.class, () -> new StateTransition("exp", T0, d, d, 0, 101, m, "a", null));
    assertThrows(IllegalArgumentException.class, () -> new StateTransition("exp", T0, d, d, 0, 0, null, "a", null));
    assertThrows(IllegalArgumentException.class, () -> new StateTransition("exp", T0, d, d, 0, 0, m, "", null));
  }

  @Test
  @DisplayName("StaleStateException names the experiment and both versions")
  public void testStaleStateMessage() {
    String message = new StaleStateException("exp", 3, 5).getMessage();

    assertTrue(message.contains("'exp'") && message.contains("version 5") && message.contains("version 3"), message);
  }
}
