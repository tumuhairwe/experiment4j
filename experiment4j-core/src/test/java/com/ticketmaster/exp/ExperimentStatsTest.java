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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExperimentStatsTest {

  @ParameterizedTest
  @EnumSource(value = MatchType.class, names = {"MATCH", "EXCEPTION_MATCH"})
  @DisplayName("Matching outcomes count as runs but not mismatches")
  public void testRecord_matchesAreNotMismatches(MatchType matchType) {
    ExperimentStats stats = new ExperimentStats();

    stats.record(matchType);

    assertEquals(1, stats.getTotalRuns());
    assertEquals(0, stats.getMismatches());
  }

  @ParameterizedTest
  @EnumSource(value = MatchType.class, names = {"MISMATCH", "EXCEPTION_MISMATCH", "CONTROL_EXCEPTION", "CANDIDATE_EXCEPTION"})
  @DisplayName("Every non-matching outcome counts as a mismatch")
  public void testRecord_nonMatchesAreMismatches(MatchType matchType) {
    ExperimentStats stats = new ExperimentStats();

    stats.record(matchType);

    assertEquals(1, stats.getTotalRuns());
    assertEquals(1, stats.getMismatches());
  }

  @Test
  @DisplayName("Concurrent records are not lost")
  public void testRecord_isThreadSafe() throws InterruptedException {
    ExperimentStats stats = new ExperimentStats();
    ExecutorService pool = Executors.newFixedThreadPool(8);

    for (int i = 0; i < 10_000; i++) {
      MatchType type = (i % 2 == 0) ? MatchType.MATCH : MatchType.MISMATCH;
      pool.submit(() -> stats.record(type));
    }
    pool.shutdown();
    assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

    assertEquals(10_000, stats.getTotalRuns());
    assertEquals(5_000, stats.getMismatches());
    assertEquals(10_000, stats.totalRunsAsInt());
    assertEquals(5_000, stats.mismatchesAsInt());
  }
}
