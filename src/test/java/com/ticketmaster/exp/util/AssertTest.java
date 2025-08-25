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

package com.ticketmaster.exp.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssertTest {

  // Removes noise from coverage results
  Assert a = new Assert();

  @Test
  public void testInRangeShouldThrowExceptionWhenEqualsUpperBound() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
      Assert.between(100, 0, 100);
    });
    // Optionally verify message
    assert ex.getMessage().contains("value 100 is not between bounds of 0 (inclusive) and 100 (exclusive)");
  }

  @Test
  public void testInRangeShouldThrowExceptionWhenOverUpperBound() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
      Assert.between(101, 0, 100);
    });
    assert ex.getMessage().contains("value 101 is not between bounds of 0 (inclusive) and 100 (exclusive)");
  }

  @Test
  public void testInRangeShouldThrowExceptionWhenUnderLowerBound() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
      Assert.between(-1, 0, 100);
    });
    assert ex.getMessage().contains("value -1 is not between bounds of 0 (inclusive) and 100 (exclusive)");
  }

  @Test
  public void testInRangeShouldNotThrowExceptionWhenInRange() {
    // MIDDLE
    Assert.between(50, 0, 100);

    // LOWER BOUND
    Assert.between(0, 0, 100);

    // UPPER BOUND
    Assert.between(99, 0, 100);
  }

  @Test
  public void testHasTextShouldThrowExceptionWhenNull() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
      Assert.hasText(null, "has text");
    });
    assertTrue(ex.getMessage().contains("has text"));
  }

  @Test
  public void testHasTextShouldThrowExceptionWhenEmpty() throws Exception {
    // EXPECT
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
      Assert.hasText(null, "has text");
    });

    // WHEN
    assert ex.getMessage().contains("has text");
  }

  @Test
  public void testHasTextShouldSucceedWhenHasText() throws Exception {
    Assert.hasText("s", "has text");
  }
}
