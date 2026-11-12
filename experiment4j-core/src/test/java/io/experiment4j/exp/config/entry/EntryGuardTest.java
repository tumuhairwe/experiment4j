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
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntryGuardTest {

  @Test
  @DisplayName("NEVER never enters and ALWAYS always enters")
  public void testConstants() {
    assertFalse(EntryGuard.NEVER.shouldEnter(0, 0, LocalDateTime.now()));
    assertTrue(EntryGuard.ALWAYS.shouldEnter(0, 0, LocalDateTime.now()));
  }

  @ParameterizedTest(name = "{0} or {1} = {2}")
  @CsvSource({"false,false,false", "false,true,true", "true,false,true", "true,true,true"})
  @DisplayName("or() enters when either guard enters")
  public void testOr(boolean first, boolean second, boolean expected) {
    EntryGuard a = first ? EntryGuard.ALWAYS : EntryGuard.NEVER;
    EntryGuard b = second ? EntryGuard.ALWAYS : EntryGuard.NEVER;

    assertEquals(expected, a.or(b).shouldEnter(0, 0, LocalDateTime.now()));
  }

  @Test
  @DisplayName("or() passes the running totals to both guards")
  public void testOrPassesTotals() {
    EntryGuard atMostFiveRuns = (runs, mismatches, time) -> runs <= 5;
    EntryGuard noMismatches = (runs, mismatches, time) -> mismatches == 0;
    EntryGuard either = atMostFiveRuns.or(noMismatches);

    assertTrue(either.shouldEnter(3, 2, null));
    assertTrue(either.shouldEnter(10, 0, null));
    assertFalse(either.shouldEnter(10, 2, null));
  }
}
