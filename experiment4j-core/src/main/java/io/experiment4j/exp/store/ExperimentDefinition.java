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
 * How an experiment is configured. Identified by its {@code name}, which matches
 * the name of the {@link io.experiment4j.exp.Experiment} that runs it; the
 * control and candidate functions themselves are registered in code and are
 * not persisted.
 *
 * @param name                   unique experiment name; required
 * @param description            what the experiment is for; optional
 * @param owner                  team or person responsible; optional
 * @param activeFrom             when the experiment may start running; optional (no start limit)
 * @param activeUntil            when the experiment must stop running; optional (no end limit)
 * @param initialTrafficPercent  share of traffic the candidate starts with, 0-100
 * @param maxTrafficPercent      share of traffic the candidate may reach, 0-100, at least the initial share
 */
public record ExperimentDefinition(
    String name,
    String description,
    String owner,
    Instant activeFrom,
    Instant activeUntil,
    int initialTrafficPercent,
    int maxTrafficPercent) {

  public ExperimentDefinition {
    Assert.hasText(name, "name must be non-null and have text");
    Assert.between(initialTrafficPercent, 0, 101);
    Assert.between(maxTrafficPercent, 0, 101);
    if (initialTrafficPercent > maxTrafficPercent) {
      throw new IllegalArgumentException("initialTrafficPercent (" + initialTrafficPercent
          + ") must not exceed maxTrafficPercent (" + maxTrafficPercent + ")");
    }
    if (activeFrom != null && activeUntil != null && activeFrom.isAfter(activeUntil)) {
      throw new IllegalArgumentException("activeFrom (" + activeFrom
          + ") must not be after activeUntil (" + activeUntil + ")");
    }
  }

  /** A definition with only a name: no window, starting at 0% and allowed to reach 100%. */
  public static ExperimentDefinition named(String name) {
    return new ExperimentDefinition(name, null, null, null, null, 0, 100);
  }

  /** Whether {@code time} is inside the active window; both ends are included. */
  public boolean isActiveAt(Instant time) {
    return (activeFrom == null || !time.isBefore(activeFrom))
        && (activeUntil == null || !time.isAfter(activeUntil));
  }
}
