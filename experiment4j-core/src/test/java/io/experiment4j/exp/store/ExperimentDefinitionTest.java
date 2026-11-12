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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExperimentDefinitionTest {

  private static final Instant NOV_1 = Instant.parse("2026-11-01T00:00:00Z");
  private static final Instant DEC_1 = Instant.parse("2026-12-01T00:00:00Z");

  @Test
  @DisplayName("named() has no window and may ramp from 0% to 100%")
  public void testNamed() {
    ExperimentDefinition definition = ExperimentDefinition.named("exp");

    assertEquals("exp", definition.name());
    assertNull(definition.activeFrom());
    assertNull(definition.activeUntil());
    assertEquals(0, definition.initialTrafficPercent());
    assertEquals(100, definition.maxTrafficPercent());
  }

  @Test
  @DisplayName("a name is required")
  public void testRequiresName() {
    assertThrows(IllegalArgumentException.class, () -> ExperimentDefinition.named(null));
    assertThrows(IllegalArgumentException.class, () -> ExperimentDefinition.named(""));
  }

  @Test
  @DisplayName("traffic shares must be 0-100 and the initial share must not exceed the maximum")
  public void testTrafficShares() {
    assertDoesNotThrow(() -> new ExperimentDefinition("exp", null, null, null, null, 100, 100));
    assertThrows(IllegalArgumentException.class, () -> new ExperimentDefinition("exp", null, null, null, null, -1, 50));
    assertThrows(IllegalArgumentException.class, () -> new ExperimentDefinition("exp", null, null, null, null, 0, 101));
    assertThrows(IllegalArgumentException.class, () -> new ExperimentDefinition("exp", null, null, null, null, 60, 50));
  }

  @Test
  @DisplayName("the active window must not end before it starts")
  public void testWindowOrder() {
    assertDoesNotThrow(() -> new ExperimentDefinition("exp", null, null, NOV_1, NOV_1, 0, 100));
    assertThrows(IllegalArgumentException.class, () -> new ExperimentDefinition("exp", null, null, DEC_1, NOV_1, 0, 100));
  }

  @Test
  @DisplayName("isActiveAt includes both ends of the window")
  public void testIsActiveAt() {
    ExperimentDefinition november = new ExperimentDefinition("exp", null, null, NOV_1, DEC_1, 0, 100);

    assertTrue(november.isActiveAt(NOV_1));
    assertTrue(november.isActiveAt(DEC_1));
    assertFalse(november.isActiveAt(NOV_1.minusNanos(1)));
    assertFalse(november.isActiveAt(DEC_1.plusNanos(1)));
  }

  @Test
  @DisplayName("a missing start or end leaves that side of the window open")
  public void testOpenWindow() {
    ExperimentDefinition fromNovember = new ExperimentDefinition("exp", null, null, NOV_1, null, 0, 100);
    ExperimentDefinition untilDecember = new ExperimentDefinition("exp", null, null, null, DEC_1, 0, 100);

    assertTrue(fromNovember.isActiveAt(Instant.MAX));
    assertFalse(fromNovember.isActiveAt(NOV_1.minusSeconds(1)));
    assertTrue(untilDecember.isActiveAt(Instant.EPOCH));
    assertFalse(untilDecember.isActiveAt(DEC_1.plusSeconds(1)));
    assertTrue(ExperimentDefinition.named("exp").isActiveAt(NOV_1));
  }
}
