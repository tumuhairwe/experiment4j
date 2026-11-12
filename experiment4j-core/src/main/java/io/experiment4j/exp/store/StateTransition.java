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
 * One change of an experiment's state, kept as an audit trail: answers
 * "why did the candidate's traffic drop to 12%?".
 *
 * @param experimentName      name of the experiment; required
 * @param at                  when the change happened; required
 * @param fromStatus          status before the change; required
 * @param toStatus            status after the change; required
 * @param fromTrafficPercent  traffic share before the change, 0-100
 * @param toTrafficPercent    traffic share after the change, 0-100
 * @param reason              why the state changed; required
 * @param actor               who or what made the change (a user, or e.g. "ramp-up"); required
 * @param detail              free-text detail, e.g. the mismatch rate observed; optional
 */
public record StateTransition(
    String experimentName,
    Instant at,
    ExperimentStatus fromStatus,
    ExperimentStatus toStatus,
    int fromTrafficPercent,
    int toTrafficPercent,
    TransitionReason reason,
    String actor,
    String detail) {

  public StateTransition {
    Assert.hasText(experimentName, "experimentName must be non-null and have text");
    Assert.notNull(at, "at must be non-null");
    Assert.notNull(fromStatus, "fromStatus must be non-null");
    Assert.notNull(toStatus, "toStatus must be non-null");
    Assert.between(fromTrafficPercent, 0, 101);
    Assert.between(toTrafficPercent, 0, 101);
    Assert.notNull(reason, "reason must be non-null");
    Assert.hasText(actor, "actor must be non-null and have text");
  }

  /**
   * The transition from {@code current} to {@code next}, timed at {@code next.updatedAt()}.
   *
   * @throws IllegalArgumentException if the two states belong to different experiments
   */
  public static StateTransition between(ExperimentState current, ExperimentState next,
                                        TransitionReason reason, String actor, String detail) {
    Assert.notNull(current, "current must be non-null");
    Assert.notNull(next, "next must be non-null");
    if (!current.experimentName().equals(next.experimentName())) {
      throw new IllegalArgumentException("cannot transition experiment '" + current.experimentName()
          + "' to the state of '" + next.experimentName() + "'");
    }
    return new StateTransition(current.experimentName(), next.updatedAt(),
        current.status(), next.status(), current.trafficPercent(), next.trafficPercent(),
        reason, actor, detail);
  }
}
