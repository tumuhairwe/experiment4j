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

package io.experiment4j.exp.config.entry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Period;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MismatchFrequencyGuardTest {

  @ParameterizedTest
  @ValueSource(doubles = {-0.01, 1.01})
  @DisplayName("frequency outside [0, 1] is rejected")
  public void testRejectsFrequencyOutOfRange(double frequency) {
    assertThrows(IllegalArgumentException.class, () -> new MismatchFrequencyGuard(frequency));
    assertThrows(IllegalArgumentException.class,
        () -> new RelativeMismatchFrequency(Period.ofDays(1), frequency));
  }

  @ParameterizedTest
  @ValueSource(doubles = {0.0, 1.0})
  @DisplayName("frequency at the bounds 0 and 1 is accepted")
  public void testAcceptsBoundaryFrequency(double frequency) {
    assertDoesNotThrow(() -> new MismatchFrequencyGuard(frequency));
    assertDoesNotThrow(() -> new RelativeMismatchFrequency(Period.ofDays(1), frequency));
  }

  @Test
  @DisplayName("enters when there have been no runs yet")
  public void testEntersWithNoRuns() {
    assertTrue(new MismatchFrequencyGuard(0.0).shouldEnter(0, 0, null));
    assertTrue(new RelativeMismatchFrequency(Period.ofDays(1), 0.0).shouldEnter(0, 0, null));
  }

  @Test
  @DisplayName("enters while the mismatch frequency is at or below the maximum")
  public void testEntersAtOrBelowMaximum() {
    EntryGuard guard = new MismatchFrequencyGuard(0.1);
    EntryGuard relative = new RelativeMismatchFrequency(Period.ofDays(1), 0.1);

    assertTrue(guard.shouldEnter(100, 5, null));
    assertTrue(guard.shouldEnter(100, 10, null));
    assertTrue(relative.shouldEnter(100, 10, null));
  }

  @Test
  @DisplayName("stops entering once the mismatch frequency exceeds the maximum")
  public void testStopsAboveMaximum() {
    assertFalse(new MismatchFrequencyGuard(0.1).shouldEnter(100, 11, null));
    assertFalse(new RelativeMismatchFrequency(Period.ofDays(1), 0.1).shouldEnter(100, 11, null));
  }
}
