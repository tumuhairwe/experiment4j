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

package io.experiment4j.exp;

/**
 * Records that the candidate threw an {@link Error} (for example a
 * {@link StackOverflowError}) rather than an {@link Exception}.
 *
 * <p>A candidate must never affect the caller, so its errors are captured
 * like its exceptions: the trial is published as
 * {@link MatchType#CANDIDATE_EXCEPTION} (or compared with the control's
 * exception) and the caller receives the result chosen by the
 * experiment's {@code returnChoice}. The original error is available as
 * {@link #getCause()}.
 */
public final class CandidateError extends RuntimeException {

  public CandidateError(Error cause) {
    super("candidate threw " + cause, cause);
  }

  @Override
  public synchronized Error getCause() {
    return (Error) super.getCause();
  }
}
