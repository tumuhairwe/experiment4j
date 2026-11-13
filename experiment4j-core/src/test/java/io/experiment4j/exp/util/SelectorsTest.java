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

package io.experiment4j.exp.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

public class SelectorsTest {
  // removes noise from coverage results
  Selectors s = new Selectors();

  private static Object objectWithHash(int hash) {
    return new Object() {
      @Override
      public int hashCode() {
        return hash;
      }
    };
  }

  /** How many of the hash values 0 .. buckets - 1 the selector made by {@code percent} selects. */
  private static long selectedBuckets(int buckets, java.util.function.IntFunction<BooleanSupplier> selectorForHash) {
    long selected = 0;
    for (int hash = 0; hash < buckets; hash++) {
      if (selectorForHash.apply(hash).getAsBoolean()) {
        selected++;
      }
    }
    return selected;
  }

  @Test
  public void testConstantThresholds() {
    assertTrue(Selectors.always().getAsBoolean());
    assertFalse(Selectors.never().getAsBoolean());
  }

  @Test
  @DisplayName("percent accepts 0-100 and permille 0-1000; anything outside is rejected")
  public void testBounds() {
    assertDoesNotThrow(() -> Selectors.percent(100));
    assertDoesNotThrow(() -> Selectors.permille(1000));
    assertDoesNotThrow(() -> Selectors.percentOfObjectHash(100, "x"));
    assertDoesNotThrow(() -> Selectors.permilleOfObjectHash(1000, "x"));
    assertThrows(IllegalArgumentException.class, () -> Selectors.percent(101));
    assertThrows(IllegalArgumentException.class, () -> Selectors.percent(-1));
    assertThrows(IllegalArgumentException.class, () -> Selectors.permille(1001));
    assertThrows(IllegalArgumentException.class, () -> Selectors.permille(-1));
    assertThrows(IllegalArgumentException.class, () -> Selectors.percentOfObjectHash(101, "x"));
    assertThrows(IllegalArgumentException.class, () -> Selectors.permilleOfObjectHash(1001, "x"));
  }

  @Test
  @DisplayName("100% and 1000 per mille always select")
  public void testFullTrafficAlwaysSelects() {
    BooleanSupplier percent = Selectors.percent(100);
    BooleanSupplier permille = Selectors.permille(1000);

    for (int i = 0; i < 10_000; i++) {
      assertTrue(percent.getAsBoolean(), "percent failed on call " + i);
      assertTrue(permille.getAsBoolean(), "permille failed on call " + i);
    }
  }

  @Test
  @DisplayName("0% and 0 per mille never select")
  public void testZeroNeverSelects() {
    BooleanSupplier percent = Selectors.percent(0);
    BooleanSupplier permille = Selectors.permille(0);

    for (int i = 0; i < 10_000; i++) {
      assertFalse(percent.getAsBoolean(), "percent failed on call " + i);
      assertFalse(permille.getAsBoolean(), "permille failed on call " + i);
    }
  }

  @Test
  @DisplayName("percent(p) selects about p% of calls at random")
  public void testPercentDistribution() {
    BooleanSupplier quarter = Selectors.percent(25);
    int calls = 100_000;
    int selected = 0;
    for (int i = 0; i < calls; i++) {
      if (quarter.getAsBoolean()) {
        selected++;
      }
    }

    // expected 25,000; the standard deviation is about 137, so this never fails by chance
    assertTrue(selected > 23_500 && selected < 26_500, "selected " + selected + " of " + calls);
  }

  @ParameterizedTest(name = "percentOfObjectHash({0}) selects exactly {0} of 100 buckets")
  @ValueSource(ints = {0, 1, 25, 50, 99, 100})
  public void testPercentOfObjectHashSelectsExactly(int percent) {
    assertEquals(percent, selectedBuckets(100, hash -> Selectors.percentOfObjectHash(percent, objectWithHash(hash))));
  }

  @ParameterizedTest(name = "permilleOfObjectHash({0}) selects exactly {0} of 1000 buckets")
  @ValueSource(ints = {0, 1, 500, 999, 1000})
  public void testPermilleOfObjectHashSelectsExactly(int permille) {
    assertEquals(permille, selectedBuckets(1000, hash -> Selectors.permilleOfObjectHash(permille, objectWithHash(hash))));
  }

  @Test
  @DisplayName("object-hash selection is sticky: the same object always gets the same answer")
  public void testObjectHashIsSticky() {
    Object user = objectWithHash(42);
    BooleanSupplier in = Selectors.percentOfObjectHash(43, user);
    BooleanSupplier out = Selectors.percentOfObjectHash(42, user);

    for (int i = 0; i < 100; i++) {
      assertTrue(in.getAsBoolean());
      assertFalse(out.getAsBoolean());
    }
  }

  @Test
  @DisplayName("negative hash codes, including Integer.MIN_VALUE, fall into a normal bucket")
  public void testNegativeHashCodes() {
    // floorMod(Integer.MIN_VALUE, 100) == 52 and floorMod(-1, 100) == 99
    assertFalse(Selectors.percentOfObjectHash(1, objectWithHash(Integer.MIN_VALUE)).getAsBoolean());
    assertFalse(Selectors.percentOfObjectHash(52, objectWithHash(Integer.MIN_VALUE)).getAsBoolean());
    assertTrue(Selectors.percentOfObjectHash(53, objectWithHash(Integer.MIN_VALUE)).getAsBoolean());
    assertFalse(Selectors.percentOfObjectHash(99, objectWithHash(-1)).getAsBoolean());
    assertTrue(Selectors.percentOfObjectHash(100, objectWithHash(-1)).getAsBoolean());
  }

  @Test
  @DisplayName("dynamic thresholds of 100 select everything; values outside the range are clamped")
  public void testDynamicThresholdsAreClamped() {
    assertTrue(Selectors.dynamicPercent(() -> 100).getAsBoolean());
    assertTrue(Selectors.dynamicPercent(() -> 150).getAsBoolean());
    assertFalse(Selectors.dynamicPercent(() -> -5).getAsBoolean());
    assertTrue(Selectors.dynamicPermille(() -> 1000).getAsBoolean());
    assertTrue(Selectors.dynamicPercentOfObjectHash(() -> 100, objectWithHash(99)).getAsBoolean());
    assertTrue(Selectors.dynamicPermilleOfObjectHash(() -> 2000, objectWithHash(999)).getAsBoolean());
    assertFalse(Selectors.dynamicPermilleOfObjectHash(() -> 0, objectWithHash(0)).getAsBoolean());
  }

  @Test
  @DisplayName("dynamic thresholds are read on every call")
  public void testDynamicThresholdIsReadEachCall() {
    AtomicInteger percent = new AtomicInteger(0);
    BooleanSupplier selector = Selectors.dynamicPercent(percent::get);

    assertFalse(selector.getAsBoolean());
    percent.set(100);
    assertTrue(selector.getAsBoolean());
  }

  @Test
  @DisplayName("ModInt maps any value, including negatives, into 0 .. denominator - 1 and rejects a zero denominator")
  public void testModInt() {
    assertEquals(52, new Selectors.ModInt(() -> Integer.MIN_VALUE, 100).getAsInt());
    assertEquals(99, new Selectors.ModInt(() -> -1, 100).getAsInt());
    assertEquals(7, new Selectors.ModInt(() -> 107, 100).getAsInt());
    assertThrows(IllegalArgumentException.class, () -> new Selectors.ModInt(() -> 1, 0));
  }
}
