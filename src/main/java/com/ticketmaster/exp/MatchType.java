/*
 * Copyright 2015 the original author or authors.
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

package com.ticketmaster.exp;

public enum MatchType {
  /** Both returned a result, and the results matched. */
  MATCH,
  /** Both returned a result, but the results did not match. */
  MISMATCH,
  /** Both threw exceptions of the same type. */
  EXCEPTION_MATCH,
  /** Both threw exceptions of different types. */
  EXCEPTION_MISMATCH,
  /** The control threw an exception, but the candidate did not. */
  CONTROL_EXCEPTION,
  /** The candidate threw an exception, but the control did not. */
  CANDIDATE_EXCEPTION;

  /**
   * Checks if this match type represents a scenario where the results or behaviors diverged.
   * @return true if the outcome is considered a mismatch, false otherwise.
   */
  public boolean isMismatch() {
    return this == MISMATCH || this == EXCEPTION_MISMATCH || this == CONTROL_EXCEPTION || this == CANDIDATE_EXCEPTION;
  }
}
