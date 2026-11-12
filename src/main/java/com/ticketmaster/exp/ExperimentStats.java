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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe, in-memory running totals for one experiment, shared by every
 * {@link Trial} the experiment creates. Entry guards read these totals to
 * decide whether a trial may enter the experiment.
 *
 * <p>Only trials in which both control and candidate ran are counted. A trial
 * counts as a mismatch unless its outcome is {@link MatchType#MATCH} or
 * {@link MatchType#EXCEPTION_MATCH}.
 *
 * <p>Reading the totals and running the trial are not atomic, so under
 * concurrent load a count-based guard may let a few extra trials through.
 */
public final class ExperimentStats {

  private final AtomicLong totalRuns = new AtomicLong();
  private final AtomicLong mismatches = new AtomicLong();

  /** Records the outcome of one trial in which both control and candidate ran. */
  public void record(MatchType matchType) {
    totalRuns.incrementAndGet();
    if (matchType != MatchType.MATCH && matchType != MatchType.EXCEPTION_MATCH) {
      mismatches.incrementAndGet();
    }
  }

  public long getTotalRuns() {
    return totalRuns.get();
  }

  public long getMismatches() {
    return mismatches.get();
  }

  /** Total runs, capped at {@link Integer#MAX_VALUE} for the int-based EntryGuard API. */
  int totalRunsAsInt() {
    return saturatedInt(totalRuns.get());
  }

  /** Mismatches, capped at {@link Integer#MAX_VALUE} for the int-based EntryGuard API. */
  int mismatchesAsInt() {
    return saturatedInt(mismatches.get());
  }

  private static int saturatedInt(long value) {
    return (int) Math.min(value, Integer.MAX_VALUE);
  }
}
