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

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;


public class TryTest {

  @Test
  public void testSuccessfulCallShouldReturnValue() throws Exception {

    // GIVEN
    Try<String> t = Try.of("s", null);

    // WHEN
    String s = t.call();

    // THEN
    assertEquals("s", s);
  }

  @Test
  public void testFailingCallWillFailWithException() throws Exception {
    // GIVEN
    Try<String> t = Try.of(null, new IllegalArgumentException("fail"));

    // WHEN + THEN
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, t::call);
    assertEquals("fail", ex.getMessage());
  }

  @Test
  public void testAllValuesShouldFail() throws Exception {
    // WHEN + THEN
    IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> Try.of("s", new IllegalArgumentException())
    );

    assertEquals("exactly one of value or exception must be non-null", ex.getMessage());
  }

  @Test
  public void testAllNullShouldFail() throws Exception {
    // WHEN + THEN
    IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> Try.of(null, null)
    );

    assertEquals("exactly one of value or exception must be non-null", ex.getMessage());
  }

  @Test
  public void testOfWithValueIsSuccess() throws Exception {
    // WHEN
    Try<String> t = Try.of("s", null);

    // THEN
    assertFalse(t.exception().isPresent());
    assertTrue(t.value().isPresent());
    assertTrue(t.isSuccess());
    assertFalse(t.isFailure());
  }

  @Test
  public void testOfWithExceptionIsFailure() throws Exception {
    // WHEN
    Try<String> t = Try.of(null, new IllegalArgumentException());

    // THEN
    assertTrue(t.exception().isPresent());
    assertFalse(t.value().isPresent());
    assertFalse(t.isSuccess());
    assertTrue(t.isFailure());
  }

  @Test
  public void testSuccessfulCallable() throws Exception {
    // GIVEN
    Callable<String> c = () -> "s";

    // WHEN
    Try<String> t = Try.from(c);

    // THEN
    assertFalse(t.exception().isPresent());
    assertTrue(t.value().isPresent());
    assertTrue(t.isSuccess());
    assertFalse(t.isFailure());
  }

  @Test
  public void testFailedCallable() throws Exception {
    // GIVEN
    Callable<String> c = () -> {
      throw new IllegalArgumentException();
    };

    // WHEN
    Try<String> t = Try.from(c);

    // THEN
    assertTrue(t.exception().isPresent());
    assertFalse(t.value().isPresent());
    assertFalse(t.isSuccess());
    assertTrue(t.isFailure());
  }

  @Test
  public void testGetOrThrowShouldThrowWhenHasException() throws Exception {
    // GIVEN
    Try<String> t = Try.of(null, new Exception("foo"));

    // WHEN + THEN
    Exception ex = assertThrows(Exception.class, t::getOrThrow);

    assertEquals("foo", ex.getMessage());
  }

  @Test
  public void testGetOrThrowUncheckedShouldThrowRuntimeExceptionWhenHasCheckedException() throws Exception {
    // GIVEN
    Try<String> t = Try.of(null, new Exception("foo"));

    // WHEN + THEN
    RuntimeException ex = assertThrows(RuntimeException.class, t::getOrThrowUnchecked);

    //assertEquals("foo", ex.getMessage());
    assertTrue(ex.getMessage().endsWith("foo"));
  }

  @Test
  public void testGetOrThrowUncheckedShouldThrowOriginalExceptionWhenItIsUnchecked() throws Exception {
    // GIVEN
    Try<String> t = Try.of(null, new IllegalArgumentException("foo"));

    // WHEN + THEN
    // WHEN + THEN
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, t::getOrThrowUnchecked);

    assertEquals("foo", ex.getMessage());
  }

  @Test
  public void testGetOrThrowMethodsShouldReturnValueWhenExists() throws Exception {
    // GIVEN
    Try<String> t = Try.of("foo", null);

    // THEN
    assertEquals("foo", t.getOrThrowUnchecked());
    assertEquals("foo", t.getOrThrow());
  }
}
