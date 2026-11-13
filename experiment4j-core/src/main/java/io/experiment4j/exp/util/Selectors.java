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

import java.time.Instant;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

public class Selectors {

  public static final int MAX_PERCENT = 100;
  public static final int MAX_PERMILLE = 1000;

  public static class ModInt implements IntSupplier {
    private final IntSupplier supplier;
    private final int denominator;

    public ModInt(IntSupplier supplier, int denominator) {
      Assert.between(denominator, 1, Integer.MAX_VALUE);
      this.supplier = supplier;
      this.denominator = denominator;
    }

    /** The supplied value mapped into {@code 0 .. denominator - 1}, also for negative values. */
    @Override
    public int getAsInt() {
      return Math.floorMod(supplier.getAsInt(), denominator);
    }
  }

  private static class RandomInts implements IntSupplier {
    private final Random ints = new Random(Instant.now().toEpochMilli());

    @Override
    public int getAsInt() {
      return ints.nextInt();
    }
  }

  /**
   * True when the value is less than the threshold. With values in {@code 0 .. D - 1}
   * and a threshold in {@code 0 .. D}, a threshold of {@code t} selects {@code t} out of
   * {@code D} values: 0 selects none, D selects all.
   */
  public static class LessThanThreshhold implements BooleanSupplier {
    private final IntSupplier valueSupplier;
    private final IntSupplier thresholdSupplier;

    @Override
    public boolean getAsBoolean() {
      int threshold = thresholdSupplier.getAsInt();
      if (threshold <= 0) {
        return false;
      } else {
        int value = valueSupplier.getAsInt();
        return value < threshold;
      }
    }

    public LessThanThreshhold(IntSupplier thresholdSupplier, IntSupplier valueSupplier) {
      this.thresholdSupplier = thresholdSupplier;
      this.valueSupplier = valueSupplier;
    }
  }

  Selectors() {
  }

  public static final BooleanSupplier NEVER = () -> false;

  public static final BooleanSupplier ALWAYS = () -> true;

  public static BooleanSupplier never() {
    return NEVER;
  }

  public static BooleanSupplier always() {
    return ALWAYS;
  }

  /**
   * Selects {@code thresholdSupplier} percent of calls at random: 0 never, 100 always.
   * The threshold is read on every call; values outside 0-100 are clamped.
   */
  public static BooleanSupplier dynamicPercent(IntSupplier thresholdSupplier) {
    return dynamicPerDenom(MAX_PERCENT, thresholdSupplier);
  }

  /**
   * Selects {@code thresholdSupplier} per mille of calls at random: 0 never, 1000 always.
   * The threshold is read on every call; values outside 0-1000 are clamped.
   */
  public static BooleanSupplier dynamicPermille(IntSupplier thresholdSupplier) {
    return dynamicPerDenom(MAX_PERMILLE, thresholdSupplier);
  }

  /**
   * Selects {@code thresholdSupplier} out of every {@code denominator} calls at random:
   * 0 never, {@code denominator} always. Thresholds outside that range are clamped.
   */
  public static BooleanSupplier dynamicPerDenom(int denominator, IntSupplier thresholdSupplier) {
    return new LessThanThreshhold(
        clamped(thresholdSupplier, denominator),
        new ModInt(new RandomInts(), denominator));
  }

  /** Selects {@code permille} out of every 1000 calls at random, 0-1000: 0 never, 1000 always. */
  public static BooleanSupplier permille(int permille) {
    Assert.between(permille, 0, MAX_PERMILLE + 1);
    return dynamicPermille(() -> permille);
  }

  /** Selects {@code percent} out of every 100 calls at random, 0-100: 0 never, 100 always. */
  public static BooleanSupplier percent(int percent) {
    Assert.between(percent, 0, MAX_PERCENT + 1);
    return dynamicPercent(() -> percent);
  }

  /**
   * Selects by the object's hash code, so the same object is always in or out
   * ("sticky"): {@code thresholdSupplier} out of every 100 hash buckets are selected.
   * Values outside 0-100 are clamped.
   */
  public static BooleanSupplier dynamicPercentOfObjectHash(
      IntSupplier thresholdSupplier, Object object) {
    return new LessThanThreshhold(
        clamped(thresholdSupplier, MAX_PERCENT),
        new ModInt(() -> object.hashCode(), MAX_PERCENT));
  }

  /**
   * Selects by the object's hash code, so the same object is always in or out
   * ("sticky"): {@code thresholdSupplier} out of every 1000 hash buckets are selected.
   * Values outside 0-1000 are clamped.
   */
  public static BooleanSupplier dynamicPermilleOfObjectHash(
      IntSupplier thresholdSupplier, Object object) {
    return new LessThanThreshhold(
        clamped(thresholdSupplier, MAX_PERMILLE),
        new ModInt(() -> object.hashCode(), MAX_PERMILLE));
  }

  /** Sticky selection of {@code percent} (0-100) out of every 100 hash buckets. */
  public static BooleanSupplier percentOfObjectHash(int percent, Object object) {
    Assert.between(percent, 0, MAX_PERCENT + 1);
    return dynamicPercentOfObjectHash(() -> percent, object);
  }

  /** Sticky selection of {@code permille} (0-1000) out of every 1000 hash buckets. */
  public static BooleanSupplier permilleOfObjectHash(int permille, Object object) {
    Assert.between(permille, 0, MAX_PERMILLE + 1);
    return dynamicPermilleOfObjectHash(() -> permille, object);
  }

  private static IntSupplier clamped(IntSupplier thresholdSupplier, int max) {
    return () -> Math.max(0, Math.min(max, thresholdSupplier.getAsInt()));
  }
}
