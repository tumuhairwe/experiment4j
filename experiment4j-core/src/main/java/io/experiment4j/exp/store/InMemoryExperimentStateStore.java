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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@link ExperimentStateStore} that keeps state and transitions in memory, for
 * one JVM. Updates are serialized, so the version check, the new state and its
 * transition are applied together.
 */
public class InMemoryExperimentStateStore implements ExperimentStateStore {

  private final Map<String, ExperimentState> states = new HashMap<>();
  private final Map<String, List<StateTransition>> transitions = new HashMap<>();

  @Override
  public synchronized Optional<ExperimentState> find(String experimentName) {
    return Optional.ofNullable(states.get(experimentName));
  }

  @Override
  public synchronized ExperimentState createIfAbsent(ExperimentState initial) {
    Assert.notNull(initial, "initial must be non-null");
    return states.computeIfAbsent(initial.experimentName(), name -> withVersion(initial, 0));
  }

  @Override
  public synchronized ExperimentState update(ExperimentState current, ExperimentState next,
                                             StateTransition transition) {
    Assert.notNull(current, "current must be non-null");
    Assert.notNull(next, "next must be non-null");
    Assert.notNull(transition, "transition must be non-null");
    String name = current.experimentName();
    if (!name.equals(next.experimentName()) || !name.equals(transition.experimentName())) {
      throw new IllegalArgumentException("current state, next state and transition must all be for experiment '"
          + name + "'");
    }
    ExperimentState stored = states.get(name);
    if (stored == null) {
      throw new IllegalStateException("experiment '" + name + "' has no state; call createIfAbsent first");
    }
    if (stored.version() != current.version()) {
      throw new StaleStateException(name, current.version(), stored.version());
    }
    ExperimentState updated = withVersion(next, current.version() + 1);
    states.put(name, updated);
    transitions.computeIfAbsent(name, n -> new ArrayList<>()).add(transition);
    return updated;
  }

  @Override
  public synchronized List<StateTransition> transitions(String experimentName) {
    return List.copyOf(transitions.getOrDefault(experimentName, List.of()));
  }

  private static ExperimentState withVersion(ExperimentState state, long version) {
    return new ExperimentState(state.experimentName(), state.status(), state.trafficPercent(),
        state.currentStep(), state.stepEnteredAt(), state.killed(), version, state.updatedAt());
  }
}
