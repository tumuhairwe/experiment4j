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

package com.ticketmaster.exp.config.rampup;

import com.ticketmaster.exp.ExperimentContext;
import com.ticketmaster.exp.config.RampUpStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.Period;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RampUpStrategyTest {

  private static ExperimentContext context(int totalRuns, int mismatches) {
    ExperimentContext context = new ExperimentContext();
    context.getTotalRuns().set(totalRuns);
    context.getMismatches().set(mismatches);
    return context;
  }

  /** runs, mismatches, threshold, expected adjustment for increment 10. */
  static Stream<Arguments> cases() {
    return Stream.of(
        Arguments.of(0, 0, 0, 0),      // no evidence yet: do not ramp up
        Arguments.of(500, 0, 0, 10),   // healthy: zero mismatches no longer divides by zero
        Arguments.of(500, 3, 0, 0),    // mismatches above threshold
        Arguments.of(500, 3, 3, 10),   // mismatches at threshold
        Arguments.of(500, 4, 3, 0),    // mismatches just above threshold
        Arguments.of(1, 0, 5, 10));    // a single clean run is enough evidence
  }

  @ParameterizedTest(name = "runs={0}, mismatches={1}, threshold={2} -> {3}")
  @MethodSource("cases")
  @DisplayName("DefaultRampUpStrategy ramps up only after a run and when mismatches <= threshold")
  public void testDefaultRampUpStrategy(int runs, int mismatches, int threshold, int expected) {
    RampUpStrategy strategy = new DefaultRampUpStrategy(10, threshold, Period.ofDays(1));

    assertEquals(expected, strategy.getAdjustment(context(runs, mismatches)));
  }

  @ParameterizedTest(name = "runs={0}, mismatches={1}, baseline={2} -> {3}")
  @MethodSource("cases")
  @DisplayName("MismatchGuageStrategy ramps up only after a run and when mismatches <= baseline")
  public void testMismatchGuageStrategy(int runs, int mismatches, int baseline, int expected) {
    RampUpStrategy strategy = new MismatchGuageStrategy(Duration.ofHours(1), baseline, 10);

    assertEquals(expected, strategy.getAdjustment(context(runs, mismatches)));
  }

  @Test
  @DisplayName("DEFAULT ramps up by 10 only when there have been runs and no mismatches")
  public void testDefaultConstant() {
    assertEquals(10, RampUpStrategy.DEFAULT.getAdjustment(context(100, 0)));
    assertEquals(0, RampUpStrategy.DEFAULT.getAdjustment(context(100, 1)));
  }

  @Test
  @DisplayName("A new ExperimentContext can be evaluated without a NullPointerException")
  public void testFreshContext() {
    ExperimentContext fresh = new ExperimentContext();

    assertEquals(0, RampUpStrategy.DEFAULT.getAdjustment(fresh));
    assertEquals(0, new MismatchGuageStrategy(Duration.ofHours(1), 0, 10).getAdjustment(fresh));
    assertEquals(0, RampUpStrategy.NO_RAMP_UP.getAdjustment(fresh));
  }
}
