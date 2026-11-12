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

import java.util.List;
import java.util.Optional;

/**
 * Stores each experiment's {@link ExperimentState} and the {@link StateTransition}s
 * that led to it. Implementations must be thread-safe, and {@link #update} must
 * be atomic: the version check, the new state and its transition are written
 * together or not at all.
 */
public interface ExperimentStateStore {

  /** The current state of the experiment named {@code experimentName}, if any. */
  Optional<ExperimentState> find(String experimentName);

  /**
   * Stores {@code initial} at version 0 if the experiment has no state yet, and
   * returns the stored state: the new one, or the existing one unchanged.
   * Safe to call from every application instance at startup.
   */
  ExperimentState createIfAbsent(ExperimentState initial);

  /**
   * Replaces {@code current} with {@code next} and records {@code transition}.
   * The stored state gets version {@code current.version() + 1}; the version of
   * {@code next} is ignored.
   *
   * @return the stored state
   * @throws StaleStateException      if the stored version is not {@code current.version()}
   * @throws IllegalStateException    if the experiment has no state
   * @throws IllegalArgumentException if the states and transition are not for the same experiment
   */
  ExperimentState update(ExperimentState current, ExperimentState next, StateTransition transition);

  /** The transitions recorded for the experiment, oldest first. */
  List<StateTransition> transitions(String experimentName);
}
