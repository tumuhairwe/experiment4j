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

/** Lifecycle of an experiment's rollout. */
public enum ExperimentStatus {
  /** Defined but not running: only the control runs. */
  DRAFT,
  /** Candidate runs alongside the control on a share of traffic; the control's result is returned. */
  SHADOW,
  /** Candidate traffic is being increased step by step. */
  RAMPING,
  /** The candidate's result is returned; the control still runs for comparison. */
  CANDIDATE_PRIMARY,
  /** Temporarily stopped: only the control runs. */
  PAUSED,
  /** Stopped because the candidate misbehaved: only the control runs. */
  ROLLED_BACK,
  /** Finished: the candidate has replaced the control. */
  COMPLETED
}
