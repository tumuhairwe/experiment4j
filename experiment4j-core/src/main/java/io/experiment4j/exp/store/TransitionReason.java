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

/** Why an experiment's state changed. */
public enum TransitionReason {
  /** Changed by a person, e.g. through an admin tool. */
  MANUAL,
  /** Changed because the experiment's active window started or ended. */
  SCHEDULE,
  /** Traffic increased automatically because the candidate was healthy. */
  AUTO_RAMP_UP,
  /** Traffic reduced automatically because mismatches rose. */
  AUTO_RAMP_DOWN,
  /** Stopped automatically because mismatches exceeded the rollback threshold. */
  AUTO_ROLLBACK,
  /** Stopped immediately with the kill switch. */
  KILL_SWITCH
}
